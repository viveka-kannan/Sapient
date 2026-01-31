package com.moviebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Movie Booking Platform - Main Application
 * 
 * This platform serves both B2B (theatre partners) and B2C (end customers) clients.
 * 
 * Features:
 * - Browse theatres running shows for a movie in a city
 * - Book tickets with seat selection
 * - Dynamic pricing with offers (afternoon discounts, bulk discounts)
 * 
 * @author Candidate
 */
@SpringBootApplication
public class MovieBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieBookingApplication.class, args);
    }
}
