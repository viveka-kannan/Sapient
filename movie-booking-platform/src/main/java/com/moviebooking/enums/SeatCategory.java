package com.moviebooking.enums;

/**
 * Enum representing seat categories with different pricing tiers
 */
public enum SeatCategory {
    REGULAR("Regular", 1.0),
    PREMIUM("Premium", 1.5),
    VIP("VIP", 2.0);

    private final String displayName;
    private final double priceMultiplier;

    SeatCategory(String displayName, double priceMultiplier) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}
