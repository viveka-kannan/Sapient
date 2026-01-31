package com.moviebooking.service;

import com.moviebooking.dto.request.BookTicketRequest;
import com.moviebooking.dto.response.BookingResponse;

/**
 * Service interface for booking tickets
 * Implements the WRITE scenario
 */
public interface BookingService {

    /**
     * Book movie tickets
     * - Validates seat availability
     * - Applies offers (afternoon discount, bulk discount)
     * - Creates booking record
     * - Updates seat status
     * 
     * @param request booking request with show, customer, and seat details
     * @return booking confirmation response
     */
    BookingResponse bookTickets(BookTicketRequest request);

    /**
     * Get booking details by reference
     * 
     * @param bookingReference unique booking reference
     * @return booking details
     */
    BookingResponse getBookingByReference(String bookingReference);

    /**
     * Cancel a booking
     * 
     * @param bookingReference unique booking reference
     * @return updated booking response
     */
    BookingResponse cancelBooking(String bookingReference);
}
