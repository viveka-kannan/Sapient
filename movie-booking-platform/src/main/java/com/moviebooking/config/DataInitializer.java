package com.moviebooking.config;

import com.moviebooking.entity.*;
import com.moviebooking.enums.SeatCategory;
import com.moviebooking.enums.SeatStatus;
import com.moviebooking.enums.ShowStatus;
import com.moviebooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data initializer to populate sample data for testing
 * This runs on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing sample data...");

        // Create Cities
        City mumbai = createCity("Mumbai", "Maharashtra", "India");
        City delhi = createCity("Delhi", "Delhi", "India");
        City bangalore = createCity("Bangalore", "Karnataka", "India");

        // Create Movies
        Movie movie1 = createMovie("Jana Nayagan", "An ordinary man becomes an unexpected figure of resistance, battling injustice and confronting his past to protect his community in a high-stakes, action-packed political drama",
                186, "Tamil", "Action Drama", "UA", "2026-01-09");
        Movie movie2 = createMovie("Inception", "A thief who steals corporate secrets through dream-sharing technology",
                148, "English", "Sci-Fi", "UA", "2024-01-15");
        Movie movie3 = createMovie("RRR", "A tale of two legendary revolutionaries and their journey",
                187, "Telugu", "Action", "UA", "2024-02-01");

        // Create Theatres in Mumbai
        Theatre pvr = createTheatre("PVR Cinemas Phoenix", "Lower Parel, Mumbai", mumbai);
        Theatre inox = createTheatre("INOX Megaplex", "Malad West, Mumbai", mumbai);

        // Create Theatres in Delhi
        Theatre pvrDelhi = createTheatre("PVR Select City Walk", "Saket, New Delhi", delhi);

        // Create Theatres in Bangalore
        Theatre pvrBangalore = createTheatre("PVR Orion Mall", "Dr Rajkumar Road, Bangalore", bangalore);

        // Create Screens for PVR Mumbai
        Screen pvrScreen1 = createScreen("Screen 1", 100, "REGULAR", pvr);
        Screen pvrScreen2 = createScreen("IMAX", 150, "IMAX", pvr);

        // Create Screens for INOX Mumbai
        Screen inoxScreen1 = createScreen("Screen 1", 120, "REGULAR", inox);
        Screen inoxScreen2 = createScreen("Gold Class", 50, "PREMIUM", inox);

        // Create Screens for other theatres
        Screen pvrDelhiScreen1 = createScreen("Screen 1", 100, "REGULAR", pvrDelhi);
        Screen pvrBangaloreScreen1 = createScreen("Screen 1", 100, "REGULAR", pvrBangalore);

        // Create Seats for screens
        createSeatsForScreen(pvrScreen1, 10, 10); // 10 rows x 10 seats
        createSeatsForScreen(pvrScreen2, 15, 10); // 15 rows x 10 seats
        createSeatsForScreen(inoxScreen1, 12, 10);
        createSeatsForScreen(inoxScreen2, 5, 10);
        createSeatsForScreen(pvrDelhiScreen1, 10, 10);
        createSeatsForScreen(pvrBangaloreScreen1, 10, 10);

        // Create Shows for today and tomorrow
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // Morning shows (not afternoon - no 20% discount)
        createShowWithSeats(movie1, pvr, pvrScreen1, today, LocalTime.of(9, 30), 148);
        createShowWithSeats(movie1, pvr, pvrScreen2, today, LocalTime.of(10, 0), 148);
        
        // Afternoon shows (12 PM - 5 PM - eligible for 20% discount)
        createShowWithSeats(movie1, pvr, pvrScreen1, today, LocalTime.of(14, 0), 148);
        createShowWithSeats(movie2, pvr, pvrScreen2, today, LocalTime.of(15, 30), 146);
        
        // Evening shows
        createShowWithSeats(movie1, pvr, pvrScreen1, today, LocalTime.of(18, 30), 148);
        createShowWithSeats(movie2, pvr, pvrScreen2, today, LocalTime.of(19, 0), 146);
        createShowWithSeats(movie3, inox, inoxScreen1, today, LocalTime.of(20, 0), 187);

        // Late Night shows (for demo when testing at night)
        createShowWithSeats(movie1, pvr, pvrScreen1, today, LocalTime.of(23, 0), 148);
        createShowWithSeats(movie1, pvr, pvrScreen2, today, LocalTime.of(23, 30), 148);
        createShowWithSeats(movie2, inox, inoxScreen1, today, LocalTime.of(23, 15), 146);

        // INOX shows
        createShowWithSeats(movie1, inox, inoxScreen1, today, LocalTime.of(11, 0), 148);
        createShowWithSeats(movie2, inox, inoxScreen2, today, LocalTime.of(14, 30), 146); // Afternoon
        createShowWithSeats(movie3, inox, inoxScreen1, today, LocalTime.of(17, 30), 187);

        // Tomorrow's shows
        createShowWithSeats(movie1, pvr, pvrScreen1, tomorrow, LocalTime.of(10, 0), 148);
        createShowWithSeats(movie1, pvr, pvrScreen1, tomorrow, LocalTime.of(14, 0), 148); // Afternoon
        createShowWithSeats(movie2, pvr, pvrScreen2, tomorrow, LocalTime.of(15, 0), 146); // Afternoon

        // Delhi shows
        createShowWithSeats(movie1, pvrDelhi, pvrDelhiScreen1, today, LocalTime.of(12, 0), 148);
        createShowWithSeats(movie2, pvrDelhi, pvrDelhiScreen1, today, LocalTime.of(16, 0), 146);

        // Bangalore shows
        createShowWithSeats(movie1, pvrBangalore, pvrBangaloreScreen1, today, LocalTime.of(13, 0), 148);
        createShowWithSeats(movie3, pvrBangalore, pvrBangaloreScreen1, today, LocalTime.of(18, 0), 187);

        log.info("Sample data initialized successfully!");
        log.info("Cities: {}", cityRepository.count());
        log.info("Movies: {}", movieRepository.count());
        log.info("Theatres: {}", theatreRepository.count());
        log.info("Screens: {}", screenRepository.count());
        log.info("Seats: {}", seatRepository.count());
        log.info("Shows: {}", showRepository.count());
        log.info("Show Seats: {}", showSeatRepository.count());
    }

    private City createCity(String name, String state, String country) {
        City city = City.builder()
                .name(name)
                .state(state)
                .country(country)
                .active(true)
                .build();
        return cityRepository.save(city);
    }

    private Movie createMovie(String title, String description, int duration, 
                              String language, String genre, String rating, String releaseDate) {
        Movie movie = Movie.builder()
                .title(title)
                .description(description)
                .durationMinutes(duration)
                .language(language)
                .genre(genre)
                .rating(rating)
                .releaseDate(releaseDate)
                .active(true)
                .build();
        return movieRepository.save(movie);
    }

    private Theatre createTheatre(String name, String address, City city) {
        Theatre theatre = Theatre.builder()
                .name(name)
                .address(address)
                .city(city)
                .contactNumber("+91-9876543210")
                .email(name.toLowerCase().replace(" ", "") + "@theatre.com")
                .active(true)
                .build();
        return theatreRepository.save(theatre);
    }

    private Screen createScreen(String name, int totalSeats, String screenType, Theatre theatre) {
        Screen screen = Screen.builder()
                .name(name)
                .totalSeats(totalSeats)
                .screenType(screenType)
                .theatre(theatre)
                .active(true)
                .build();
        return screenRepository.save(screen);
    }

    private void createSeatsForScreen(Screen screen, int rows, int seatsPerRow) {
        List<Seat> seats = new ArrayList<>();
        
        for (int row = 0; row < rows; row++) {
            String rowLetter = String.valueOf((char) ('A' + row));
            SeatCategory category;
            double basePrice;

            // First 2 rows are VIP, next 3 are Premium, rest are Regular
            if (row < 2) {
                category = SeatCategory.VIP;
                basePrice = 500.0;
            } else if (row < 5) {
                category = SeatCategory.PREMIUM;
                basePrice = 350.0;
            } else {
                category = SeatCategory.REGULAR;
                basePrice = 200.0;
            }

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = Seat.builder()
                        .rowNumber(rowLetter)
                        .seatNumber(seatNum)
                        .category(category)
                        .basePrice(basePrice)
                        .screen(screen)
                        .build();
                seats.add(seat);
            }
        }
        
        seatRepository.saveAll(seats);
    }

    private void createShowWithSeats(Movie movie, Theatre theatre, Screen screen, 
                                      LocalDate date, LocalTime startTime, int durationMinutes) {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        Show show = Show.builder()
                .movie(movie)
                .theatre(theatre)
                .screen(screen)
                .showDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(ShowStatus.OPEN_FOR_BOOKING)
                .availableSeats(screen.getTotalSeats())
                .build();

        show = showRepository.save(show);

        // Create show seats
        List<Seat> seats = seatRepository.findByScreenIdOrderByRowNumberAscSeatNumberAsc(screen.getId());
        List<ShowSeat> showSeats = new ArrayList<>();

        for (Seat seat : seats) {
            ShowSeat showSeat = ShowSeat.builder()
                    .show(show)
                    .seat(seat)
                    .status(SeatStatus.AVAILABLE)
                    .price(seat.getBasePrice())
                    .build();
            showSeats.add(showSeat);
        }

        showSeatRepository.saveAll(showSeats);
    }
}
