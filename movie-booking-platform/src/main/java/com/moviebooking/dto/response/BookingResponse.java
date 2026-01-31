package com.moviebooking.dto.response;

import lombok.*;
import java.util.List;

/**
 * Response DTO for successful booking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private String bookingReference;
    private String status;
    private String paymentStatus;
    private String bookingTime;

    // Customer Details
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Show Details
    private ShowDetails showDetails;

    // Seat Details
    private List<SeatInfo> seats;

    // Pricing Details
    private PricingDetails pricing;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShowDetails {
        private Long showId;
        private String movieTitle;
        private String theatreName;
        private String screenName;
        private String showDate;
        private String showTime;
        private String city;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatInfo {
        private Long seatId;
        private String seatIdentifier;
        private String category;
        private Double price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PricingDetails {
        private Double baseAmount;
        private Double discountAmount;
        private String discountDescription;
        private Double finalAmount;
        private List<AppliedOffer> appliedOffers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppliedOffer {
        private String offerName;
        private Double discountAmount;
    }
}
