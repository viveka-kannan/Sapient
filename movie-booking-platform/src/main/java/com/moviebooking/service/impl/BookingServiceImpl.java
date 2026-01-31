package com.moviebooking.service.impl;

import com.moviebooking.dto.request.BookTicketRequest;
import com.moviebooking.dto.response.BookingResponse;
import com.moviebooking.dto.response.BookingResponse.*;
import com.moviebooking.entity.*;
import com.moviebooking.enums.BookingStatus;
import com.moviebooking.enums.PaymentStatus;
import com.moviebooking.enums.SeatStatus;
import com.moviebooking.exception.BookingException;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.exception.SeatNotAvailableException;
import com.moviebooking.repository.*;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of BookingService
 * 
 * This service handles the WRITE scenario:
 * - Book movie tickets with seat selection
 * - Apply pricing and discounts
 * - Handle concurrent booking with pessimistic locking
 * 
 * Design Patterns Used:
 * - Service Layer Pattern
 * - Transaction Script Pattern
 * - Builder Pattern for response construction
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final PricingService pricingService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Override
    @Transactional
    public BookingResponse bookTickets(BookTicketRequest request) {
        log.info("Processing booking request for show: {}, seats: {}", 
                 request.getShowId(), request.getSeatIds());

        // 1. Validate show exists and is open for booking
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.getShowId()));

        validateShowForBooking(show);

        // 2. Lock and validate seats are available (pessimistic locking to prevent race conditions)
        List<ShowSeat> seatsToBook = showSeatRepository.findAvailableSeatsForBooking(
                request.getShowId(), 
                request.getSeatIds()
        );

        if (seatsToBook.size() != request.getSeatIds().size()) {
            int requestedCount = request.getSeatIds().size();
            int availableCount = seatsToBook.size();
            throw new SeatNotAvailableException(
                String.format("Some seats are no longer available. Requested: %d, Available: %d", 
                             requestedCount, availableCount)
            );
        }

        // 3. Calculate pricing with offers
        List<Double> seatPrices = seatsToBook.stream()
                .map(ShowSeat::getPrice)
                .collect(Collectors.toList());

        PricingService.PricingResult pricingResult = pricingService.calculatePricing(
                seatPrices, 
                show.isAfternoonShow()
        );

        // 4. Create booking record
        String bookingReference = generateBookingReference();
        
        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .numberOfSeats(seatsToBook.size())
                .baseAmount(pricingResult.baseAmount())
                .discountAmount(pricingResult.discountAmount())
                .finalAmount(pricingResult.finalAmount())
                .discountDescription(pricingResult.discountDescription())
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PENDING) // Payment integration would update this
                .bookingTime(LocalDateTime.now())
                .show(show)
                .build();

        booking = bookingRepository.save(booking);

        // 5. Update seat status to BOOKED
        for (ShowSeat showSeat : seatsToBook) {
            showSeat.setStatus(SeatStatus.BOOKED);
            showSeat.setBooking(booking);
        }
        showSeatRepository.saveAll(seatsToBook);

        // 6. Update show available seats count
        int newAvailableSeats = show.getAvailableSeats() - seatsToBook.size();
        show.setAvailableSeats(newAvailableSeats);
        showRepository.save(show);

        log.info("Booking successful. Reference: {}, Final Amount: {}", 
                 bookingReference, pricingResult.finalAmount());

        // 7. Build and return response
        return buildBookingResponse(booking, show, seatsToBook, pricingResult);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        log.info("Fetching booking details for reference: {}", bookingReference);

        Booking booking = bookingRepository.findByBookingReferenceWithDetails(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));

        Show show = booking.getShow();
        List<ShowSeat> bookedSeats = showSeatRepository.findByShowIdAndSeatIds(
                show.getId(),
                booking.getBookedSeats().stream()
                        .map(ss -> ss.getSeat().getId())
                        .collect(Collectors.toList())
        );

        // Rebuild pricing result from stored values
        PricingService.PricingResult pricingResult = new PricingService.PricingResult(
                booking.getBaseAmount(),
                booking.getDiscountAmount(),
                booking.getFinalAmount(),
                booking.getDiscountDescription(),
                List.of()
        );

        return buildBookingResponse(booking, show, bookedSeats, pricingResult);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(String bookingReference) {
        log.info("Cancelling booking: {}", bookingReference);

        Booking booking = bookingRepository.findByBookingReferenceWithDetails(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingException("Cannot cancel a completed booking");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        bookingRepository.save(booking);

        // Release the seats
        List<ShowSeat> bookedSeats = new java.util.ArrayList<>(booking.getBookedSeats());
        for (ShowSeat showSeat : bookedSeats) {
            showSeat.setStatus(SeatStatus.AVAILABLE);
            showSeat.setBooking(null);
        }
        showSeatRepository.saveAll(bookedSeats);

        // Update show available seats
        Show show = booking.getShow();
        show.setAvailableSeats(show.getAvailableSeats() + bookedSeats.size());
        showRepository.save(show);

        log.info("Booking cancelled successfully: {}", bookingReference);

        PricingService.PricingResult pricingResult = new PricingService.PricingResult(
                booking.getBaseAmount(),
                booking.getDiscountAmount(),
                booking.getFinalAmount(),
                booking.getDiscountDescription(),
                List.of()
        );

        return buildBookingResponse(booking, show, bookedSeats, pricingResult);
    }

    private void validateShowForBooking(Show show) {
        switch (show.getStatus()) {
            case CANCELLED -> throw new BookingException("This show has been cancelled");
            case COMPLETED -> throw new BookingException("This show has already been completed");
            case HOUSEFULL -> throw new BookingException("This show is housefull");
            default -> {} // OPEN_FOR_BOOKING, ALMOST_FULL, SCHEDULED are valid
        }

        // Check if show date/time has passed
        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getStartTime());
        if (showDateTime.isBefore(LocalDateTime.now())) {
            throw new BookingException("Cannot book tickets for a past show");
        }
    }

    private String generateBookingReference() {
        // Format: BK + timestamp + random suffix
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "BK" + timestamp + random;
    }

    private BookingResponse buildBookingResponse(
            Booking booking, 
            Show show, 
            List<ShowSeat> seats,
            PricingService.PricingResult pricingResult) {

        ShowDetails showDetails = ShowDetails.builder()
                .showId(show.getId())
                .movieTitle(show.getMovie().getTitle())
                .theatreName(show.getTheatre().getName())
                .screenName(show.getScreen().getName())
                .showDate(show.getShowDate().format(DATE_FORMATTER))
                .showTime(show.getStartTime().format(TIME_FORMATTER))
                .city(show.getTheatre().getCity().getName())
                .build();

        List<SeatInfo> seatInfoList = seats.stream()
                .map(ss -> SeatInfo.builder()
                        .seatId(ss.getSeat().getId())
                        .seatIdentifier(ss.getSeat().getSeatIdentifier())
                        .category(ss.getSeat().getCategory().getDisplayName())
                        .price(ss.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<AppliedOffer> appliedOffers = pricingResult.appliedOffers().stream()
                .map(ao -> AppliedOffer.builder()
                        .offerName(ao.offerName())
                        .discountAmount(ao.discountAmount())
                        .build())
                .collect(Collectors.toList());

        PricingDetails pricingDetails = PricingDetails.builder()
                .baseAmount(pricingResult.baseAmount())
                .discountAmount(pricingResult.discountAmount())
                .discountDescription(pricingResult.discountDescription())
                .finalAmount(pricingResult.finalAmount())
                .appliedOffers(appliedOffers)
                .build();

        return BookingResponse.builder()
                .bookingReference(booking.getBookingReference())
                .status(booking.getStatus().getDisplayName())
                .paymentStatus(booking.getPaymentStatus().getDisplayName())
                .bookingTime(booking.getBookingTime().format(DATETIME_FORMATTER))
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .customerPhone(booking.getCustomerPhone())
                .showDetails(showDetails)
                .seats(seatInfoList)
                .pricing(pricingDetails)
                .build();
    }
}
