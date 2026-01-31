package com.moviebooking.service.impl;

import com.moviebooking.dto.request.BrowseShowsRequest;
import com.moviebooking.dto.response.BrowseShowsResponse;
import com.moviebooking.dto.response.BrowseShowsResponse.*;
import com.moviebooking.dto.response.ShowSeatsResponse;
import com.moviebooking.dto.response.ShowSeatsResponse.*;
import com.moviebooking.entity.*;
import com.moviebooking.enums.SeatStatus;
import com.moviebooking.enums.ShowStatus;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.*;
import com.moviebooking.service.ShowBrowsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ShowBrowsingService
 * 
 * This service handles the READ scenario:
 * - Browse theatres running a movie in a city on a specific date
 * - Get seat availability for a show
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShowBrowsingServiceImpl implements ShowBrowsingService {

    private final MovieRepository movieRepository;
    private final CityRepository cityRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public BrowseShowsResponse browseShows(BrowseShowsRequest request) {
        log.info("Browsing shows for movie: {}, city: {}, date: {}", 
                 request.getMovieId(), request.getCityName(), request.getDate());

        // Validate movie exists
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));

        // Validate city exists
        City city = cityRepository.findByNameIgnoreCase(request.getCityName())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + request.getCityName()));

        // Get all shows for this movie in the city on the given date
        List<ShowStatus> validStatuses = List.of(
            ShowStatus.OPEN_FOR_BOOKING, 
            ShowStatus.ALMOST_FULL, 
            ShowStatus.SCHEDULED
        );

        List<Show> shows = showRepository.findShowsByMovieAndCityAndDate(
                request.getMovieId(),
                city.getId(),
                request.getDate(),
                validStatuses
        );

        // Group shows by theatre
        Map<Theatre, List<Show>> showsByTheatre = shows.stream()
                .collect(Collectors.groupingBy(Show::getTheatre));

        // Build response
        List<TheatreShowInfo> theatreInfoList = new ArrayList<>();

        for (Map.Entry<Theatre, List<Show>> entry : showsByTheatre.entrySet()) {
            Theatre theatre = entry.getKey();
            List<Show> theatreShows = entry.getValue();

            List<ShowTimingInfo> showTimings = theatreShows.stream()
                    .map(this::mapToShowTimingInfo)
                    .sorted(Comparator.comparing(ShowTimingInfo::getStartTime))
                    .collect(Collectors.toList());

            TheatreShowInfo theatreInfo = TheatreShowInfo.builder()
                    .theatreId(theatre.getId())
                    .theatreName(theatre.getName())
                    .address(theatre.getAddress())
                    .showTimings(showTimings)
                    .build();

            theatreInfoList.add(theatreInfo);
        }

        // Sort theatres by name
        theatreInfoList.sort(Comparator.comparing(TheatreShowInfo::getTheatreName));

        MovieInfo movieInfo = MovieInfo.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .durationMinutes(movie.getDurationMinutes())
                .rating(movie.getRating())
                .build();

        return BrowseShowsResponse.builder()
                .movie(movieInfo)
                .city(city.getName())
                .date(request.getDate().format(DATE_FORMATTER))
                .theatres(theatreInfoList)
                .totalTheatres(theatreInfoList.size())
                .totalShows(shows.size())
                .build();
    }

    @Override
    public ShowSeatsResponse getShowSeats(Long showId) {
        log.info("Getting seat availability for show: {}", showId);

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdOrderBySeatRowNumberAscSeatSeatNumberAsc(showId);

        // Group seats by row
        Map<String, List<ShowSeat>> seatsByRow = showSeats.stream()
                .collect(Collectors.groupingBy(
                        ss -> ss.getSeat().getRowNumber(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<SeatRow> seatLayout = new ArrayList<>();
        int totalSeats = 0;
        int availableSeats = 0;
        int bookedSeats = 0;
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0;

        for (Map.Entry<String, List<ShowSeat>> entry : seatsByRow.entrySet()) {
            List<SeatDetail> seatDetails = entry.getValue().stream()
                    .map(ss -> {
                        return SeatDetail.builder()
                                .showSeatId(ss.getId())
                                .seatId(ss.getSeat().getId())
                                .seatNumber(ss.getSeat().getSeatNumber())
                                .category(ss.getSeat().getCategory().getDisplayName())
                                .status(ss.getStatus().getDisplayName())
                                .price(ss.getPrice())
                                .build();
                    })
                    .sorted(Comparator.comparing(SeatDetail::getSeatNumber))
                    .collect(Collectors.toList());

            seatLayout.add(SeatRow.builder()
                    .rowNumber(entry.getKey())
                    .seats(seatDetails)
                    .build());

            // Calculate summary
            for (ShowSeat ss : entry.getValue()) {
                totalSeats++;
                if (ss.getStatus() == SeatStatus.AVAILABLE) {
                    availableSeats++;
                    minPrice = Math.min(minPrice, ss.getPrice());
                    maxPrice = Math.max(maxPrice, ss.getPrice());
                } else if (ss.getStatus() == SeatStatus.BOOKED) {
                    bookedSeats++;
                }
            }
        }

        // Build offers list
        List<AvailableOffer> offers = buildAvailableOffers(show.isAfternoonShow());

        SeatSummary summary = SeatSummary.builder()
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .bookedSeats(bookedSeats)
                .minPrice(minPrice == Double.MAX_VALUE ? 0 : minPrice)
                .maxPrice(maxPrice)
                .build();

        return ShowSeatsResponse.builder()
                .showId(showId)
                .movieTitle(show.getMovie().getTitle())
                .theatreName(show.getTheatre().getName())
                .screenName(show.getScreen().getName())
                .showDate(show.getShowDate().format(DATE_FORMATTER))
                .showTime(show.getStartTime().format(TIME_FORMATTER))
                .isAfternoonShow(show.isAfternoonShow())
                .seatLayout(seatLayout)
                .summary(summary)
                .offers(offers)
                .build();
    }

    private ShowTimingInfo mapToShowTimingInfo(Show show) {
        Double startingPrice = showSeatRepository.findMinPriceByShowId(show.getId()).orElse(0.0);
        
        List<OfferInfo> offers = buildOfferInfoList(show.isAfternoonShow());

        return ShowTimingInfo.builder()
                .showId(show.getId())
                .startTime(show.getStartTime().format(TIME_FORMATTER))
                .endTime(show.getEndTime().format(TIME_FORMATTER))
                .screenName(show.getScreen().getName())
                .screenType(show.getScreen().getScreenType())
                .availableSeats(show.getAvailableSeats())
                .status(show.getStatus().getDisplayName())
                .startingPrice(startingPrice)
                .afternoonShow(show.isAfternoonShow())
                .applicableOffers(offers)
                .build();
    }

    private List<OfferInfo> buildOfferInfoList(boolean isAfternoonShow) {
        List<OfferInfo> offers = new ArrayList<>();

        // Always available: 50% off on 3rd ticket
        offers.add(OfferInfo.builder()
                .offerCode("THIRD_TICKET_50")
                .description("50% off on 3rd ticket")
                .discountType("PERCENTAGE")
                .discountValue(50.0)
                .build());

        // Afternoon show discount
        if (isAfternoonShow) {
            offers.add(OfferInfo.builder()
                    .offerCode("AFTERNOON_20")
                    .description("20% off on afternoon shows")
                    .discountType("PERCENTAGE")
                    .discountValue(20.0)
                    .build());
        }

        return offers;
    }

    private List<AvailableOffer> buildAvailableOffers(boolean isAfternoonShow) {
        List<AvailableOffer> offers = new ArrayList<>();

        offers.add(AvailableOffer.builder()
                .offerCode("THIRD_TICKET_50")
                .description("Book 3 or more tickets and get 50% off on the 3rd ticket")
                .termsAndConditions("Applicable on booking of 3 or more tickets. Discount applied to the cheapest ticket.")
                .build());

        if (isAfternoonShow) {
            offers.add(AvailableOffer.builder()
                    .offerCode("AFTERNOON_20")
                    .description("20% off on all tickets for afternoon shows (12 PM - 5 PM)")
                    .termsAndConditions("Automatically applied for shows between 12:00 PM and 5:00 PM.")
                    .build());
        }

        return offers;
    }
}
