package com.moviebooking.dto.response;

import lombok.*;
import java.util.List;

/**
 * Response DTO for show seat availability
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeatsResponse {

    private Long showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String showDate;
    private String showTime;
    private boolean isAfternoonShow;
    
    private List<SeatRow> seatLayout;
    private SeatSummary summary;
    private List<AvailableOffer> offers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatRow {
        private String rowNumber;
        private List<SeatDetail> seats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatDetail {
        private Long showSeatId;
        private Long seatId;
        private Integer seatNumber;
        private String category;
        private String status;
        private Double price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatSummary {
        private Integer totalSeats;
        private Integer availableSeats;
        private Integer bookedSeats;
        private Double minPrice;
        private Double maxPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailableOffer {
        private String offerCode;
        private String description;
        private String termsAndConditions;
    }
}
