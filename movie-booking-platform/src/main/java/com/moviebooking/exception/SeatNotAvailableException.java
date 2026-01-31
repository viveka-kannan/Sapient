package com.moviebooking.exception;

/**
 * Exception thrown when seats are not available for booking
 */
public class SeatNotAvailableException extends RuntimeException {

    public SeatNotAvailableException(String message) {
        super(message);
    }

    public SeatNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
