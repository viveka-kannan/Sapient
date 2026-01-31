package com.moviebooking.controller;

import com.moviebooking.dto.request.BrowseShowsRequest;
import com.moviebooking.dto.response.ApiResponse;
import com.moviebooking.dto.response.BrowseShowsResponse;
import com.moviebooking.dto.response.ShowSeatsResponse;
import com.moviebooking.service.ShowBrowsingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller for browsing shows and theatres
 * Implements the READ scenario APIs
 */
@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Show Browsing", description = "APIs for browsing movies, shows, and theatres")
public class ShowBrowsingController {

    private final ShowBrowsingService showBrowsingService;

    /**
     * Browse theatres running a movie in a city on a specific date
     * 
     * READ Scenario: Browse theatres currently running the show (movie selected) 
     * in the town, including show timing by a chosen date
     */
    @GetMapping("/browse")
    @Operation(
        summary = "Browse theatres running a movie",
        description = "Get list of theatres showing a specific movie in a city on a given date, " +
                      "including show timings, available seats, and applicable offers"
    )
    public ResponseEntity<ApiResponse<BrowseShowsResponse>> browseShows(
            @Parameter(description = "Movie ID", required = true)
            @RequestParam Long movieId,
            
            @Parameter(description = "City name", required = true)
            @RequestParam String city,
            
            @Parameter(description = "Show date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("API: Browse shows - movieId: {}, city: {}, date: {}", movieId, city, date);

        BrowseShowsRequest request = BrowseShowsRequest.builder()
                .movieId(movieId)
                .cityName(city)
                .date(date)
                .build();

        BrowseShowsResponse response = showBrowsingService.browseShows(request);

        return ResponseEntity.ok(ApiResponse.success(response, 
            "Found " + response.getTotalShows() + " shows across " + response.getTotalTheatres() + " theatres"));
    }

    /**
     * Get seat availability for a specific show
     */
    @GetMapping("/{showId}/seats")
    @Operation(
        summary = "Get show seat availability",
        description = "Get detailed seat layout and availability for a specific show, " +
                      "including pricing and applicable offers"
    )
    public ResponseEntity<ApiResponse<ShowSeatsResponse>> getShowSeats(
            @Parameter(description = "Show ID", required = true)
            @PathVariable Long showId) {

        log.info("API: Get seats for show: {}", showId);

        ShowSeatsResponse response = showBrowsingService.getShowSeats(showId);

        return ResponseEntity.ok(ApiResponse.success(response, "Seat availability retrieved successfully"));
    }
}
