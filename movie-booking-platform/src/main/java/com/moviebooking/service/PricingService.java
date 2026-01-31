package com.moviebooking.service;

import java.util.List;

/**
 * Service interface for pricing and offers calculation
 * Implements the offer/discount logic:
 * - 50% discount on the third ticket
 * - 20% discount for afternoon shows
 */
public interface PricingService {

    /**
     * Calculate pricing with applicable offers
     * 
     * @param seatPrices list of individual seat prices
     * @param isAfternoonShow whether the show is in the afternoon (12 PM - 5 PM)
     * @return pricing result with base amount, discounts, and final amount
     */
    PricingResult calculatePricing(List<Double> seatPrices, boolean isAfternoonShow);

    /**
     * Pricing result with all calculations
     */
    record PricingResult(
        double baseAmount,
        double discountAmount,
        double finalAmount,
        String discountDescription,
        List<AppliedOffer> appliedOffers
    ) {}

    /**
     * Applied offer details
     */
    record AppliedOffer(
        String offerName,
        double discountAmount
    ) {}
}
