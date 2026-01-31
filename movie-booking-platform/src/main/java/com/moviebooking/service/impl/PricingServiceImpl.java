package com.moviebooking.service.impl;

import com.moviebooking.service.PricingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of PricingService
 * 
 * Offer Rules:
 * 1. 50% discount on the third ticket (applied to the cheapest ticket if 3+ tickets)
 * 2. 20% discount on all tickets for afternoon shows (12 PM - 5 PM)
 * 
 * Design Pattern: Strategy Pattern can be extended for more complex offer rules
 */
@Service
@Slf4j
public class PricingServiceImpl implements PricingService {

    private static final double THIRD_TICKET_DISCOUNT_PERCENT = 50.0;
    private static final double AFTERNOON_SHOW_DISCOUNT_PERCENT = 20.0;
    private static final int MIN_TICKETS_FOR_BULK_DISCOUNT = 3;

    @Override
    public PricingResult calculatePricing(List<Double> seatPrices, boolean isAfternoonShow) {
        if (seatPrices == null || seatPrices.isEmpty()) {
            return new PricingResult(0, 0, 0, "", List.of());
        }

        double baseAmount = seatPrices.stream().mapToDouble(Double::doubleValue).sum();
        double totalDiscount = 0;
        List<AppliedOffer> appliedOffers = new ArrayList<>();
        StringBuilder discountDescription = new StringBuilder();

        // Apply afternoon show discount first (20% off all tickets)
        if (isAfternoonShow) {
            double afternoonDiscount = baseAmount * (AFTERNOON_SHOW_DISCOUNT_PERCENT / 100);
            totalDiscount += afternoonDiscount;
            appliedOffers.add(new AppliedOffer(
                "Afternoon Show Discount (20% off)",
                Math.round(afternoonDiscount * 100.0) / 100.0
            ));
            discountDescription.append("20% Afternoon Discount");
            log.info("Applied afternoon discount: {} on base amount: {}", afternoonDiscount, baseAmount);
        }

        // Apply 50% discount on third ticket (cheapest one)
        if (seatPrices.size() >= MIN_TICKETS_FOR_BULK_DISCOUNT) {
            // Sort prices to find the cheapest ticket for the third ticket discount
            List<Double> sortedPrices = seatPrices.stream()
                    .sorted(Comparator.naturalOrder())
                    .toList();
            
            // Apply discount to the cheapest ticket (which becomes the "third" ticket)
            double cheapestTicketPrice = sortedPrices.get(0);
            double thirdTicketDiscount = cheapestTicketPrice * (THIRD_TICKET_DISCOUNT_PERCENT / 100);
            
            // If afternoon discount was applied, calculate on the discounted price
            if (isAfternoonShow) {
                thirdTicketDiscount = cheapestTicketPrice * (1 - AFTERNOON_SHOW_DISCOUNT_PERCENT / 100) 
                                      * (THIRD_TICKET_DISCOUNT_PERCENT / 100);
            }
            
            totalDiscount += thirdTicketDiscount;
            appliedOffers.add(new AppliedOffer(
                "50% off on 3rd Ticket",
                Math.round(thirdTicketDiscount * 100.0) / 100.0
            ));
            
            if (discountDescription.length() > 0) {
                discountDescription.append(" + ");
            }
            discountDescription.append("50% off 3rd ticket");
            log.info("Applied 3rd ticket discount: {} on cheapest ticket: {}", thirdTicketDiscount, cheapestTicketPrice);
        }

        double finalAmount = baseAmount - totalDiscount;
        
        // Round to 2 decimal places
        totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;
        finalAmount = Math.round(finalAmount * 100.0) / 100.0;

        log.info("Pricing calculated - Base: {}, Discount: {}, Final: {}", 
                 baseAmount, totalDiscount, finalAmount);

        return new PricingResult(
            baseAmount,
            totalDiscount,
            finalAmount,
            discountDescription.toString(),
            appliedOffers
        );
    }
}
