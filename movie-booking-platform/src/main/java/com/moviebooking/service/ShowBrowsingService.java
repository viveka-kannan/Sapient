package com.moviebooking.service;

import com.moviebooking.dto.request.BrowseShowsRequest;
import com.moviebooking.dto.response.BrowseShowsResponse;
import com.moviebooking.dto.response.ShowSeatsResponse;

/**
 * Service interface for browsing shows and theatres
 * Implements the READ scenario
 */
public interface ShowBrowsingService {

    /**
     * Browse theatres running a specific movie in a city on a given date
     * 
     * @param request contains movieId, cityName, and date
     * @return response with theatres and their show timings
     */
    BrowseShowsResponse browseShows(BrowseShowsRequest request);

    /**
     * Get seat availability for a specific show
     * 
     * @param showId the show ID
     * @return response with seat layout and availability
     */
    ShowSeatsResponse getShowSeats(Long showId);
}
