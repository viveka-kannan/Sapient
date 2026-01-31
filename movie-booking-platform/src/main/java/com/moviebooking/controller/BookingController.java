package com.moviebooking.controller;

import com.moviebooking.dto.request.BookTicketRequest;
import com.moviebooking.dto.response.ApiResponse;
import com.moviebooking.dto.response.BookingResponse;
import com.moviebooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for booking tickets
 * Implements the WRITE scenario APIs
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ticket Booking", description = "APIs for booking and managing movie tickets")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Book movie tickets
     * 
     * WRITE Scenario: Book movie tickets by selecting a theatre, timing, 
     * and preferred seats for the day
     * 
     * Offers Applied:
     * - 50% discount on the 3rd ticket (when booking 3+ tickets)
     * - 20% discount for afternoon shows (12 PM - 5 PM)
     */
    @PostMapping
    @Operation(
        summary = "Book movie tickets",
        description = "Book tickets for a show by selecting preferred seats. " +
                      "Applicable offers are automatically applied: " +
                      "50% off on 3rd ticket, 20% off for afternoon shows"
    )
    public ResponseEntity<ApiResponse<BookingResponse>> bookTickets(
            @Valid @RequestBody BookTicketRequest request) {

        log.info("API: Book tickets - showId: {}, seats: {}", 
                 request.getShowId(), request.getSeatIds().size());

        BookingResponse response = bookingService.bookTickets(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Booking confirmed! Reference: " + response.getBookingReference()));
    }

    /**
     * Get booking details by reference
     */
    @GetMapping("/{bookingReference}")
    @Operation(
        summary = "Get booking details",
        description = "Retrieve booking details using the booking reference number"
    )
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @Parameter(description = "Booking reference number", required = true)
            @PathVariable String bookingReference) {

        log.info("API: Get booking - reference: {}", bookingReference);

        BookingResponse response = bookingService.getBookingByReference(bookingReference);

        return ResponseEntity.ok(ApiResponse.success(response, "Booking details retrieved successfully"));
    }

    /**
     * Cancel a booking
     */
    @DeleteMapping("/{bookingReference}")
    @Operation(
        summary = "Cancel booking",
        description = "Cancel an existing booking. Seats will be released and refund will be initiated"
    )
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @Parameter(description = "Booking reference number", required = true)
            @PathVariable String bookingReference) {

        log.info("API: Cancel booking - reference: {}", bookingReference);

        BookingResponse response = bookingService.cancelBooking(bookingReference);

        return ResponseEntity.ok(ApiResponse.success(response, "Booking cancelled successfully"));
    }
}
