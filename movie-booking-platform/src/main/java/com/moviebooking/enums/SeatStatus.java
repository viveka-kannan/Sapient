package com.moviebooking.enums;

/**
 * Enum representing the status of a seat for a particular show
 */
public enum SeatStatus {
    AVAILABLE("Available"),
    BLOCKED("Blocked"),      // Temporarily blocked during booking process
    BOOKED("Booked"),        // Successfully booked
    UNAVAILABLE("Unavailable"); // Seat not available for this show

    private final String displayName;

    SeatStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
