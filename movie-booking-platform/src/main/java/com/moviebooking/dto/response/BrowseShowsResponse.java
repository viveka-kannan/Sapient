package com.moviebooking.dto.response;

import lombok.*;
import java.util.List;

/**
 * Response DTO for browse shows - theatres with show timings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrowseShowsResponse {

    private MovieInfo movie;
    private String city;
    private String date;
    private List<TheatreShowInfo> theatres;
    private int totalTheatres;
    private int totalShows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieInfo {
        private Long id;
        private String title;
        private String language;
        private String genre;
        private Integer durationMinutes;
        private String rating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TheatreShowInfo {
        private Long theatreId;
        private String theatreName;
        private String address;
        private List<ShowTimingInfo> showTimings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShowTimingInfo {
        private Long showId;
        private String startTime;
        private String endTime;
        private String screenName;
        private String screenType;
        private Integer availableSeats;
        private String status;
        private Double startingPrice;
        private boolean afternoonShow;
        private List<OfferInfo> applicableOffers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OfferInfo {
        private String offerCode;
        private String description;
        private String discountType;
        private Double discountValue;
    }
}
