package com.moviebooking.enums;

/**
 * Enum representing the status of a show
 */
public enum ShowStatus {
    SCHEDULED("Scheduled"),
    OPEN_FOR_BOOKING("Open for Booking"),
    ALMOST_FULL("Almost Full"),
    HOUSEFULL("Housefull"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String displayName;

    ShowStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
