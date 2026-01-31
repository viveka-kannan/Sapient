package com.moviebooking.repository;

import com.moviebooking.entity.Booking;
import com.moviebooking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    List<Booking> findByCustomerEmail(String customerEmail);
    
    List<Booking> findByShowIdAndStatus(Long showId, BookingStatus status);
    
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.show s " +
           "JOIN FETCH s.movie " +
           "JOIN FETCH s.theatre t " +
           "JOIN FETCH t.city " +
           "WHERE b.bookingReference = :reference")
    Optional<Booking> findByBookingReferenceWithDetails(@Param("reference") String reference);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.show.id = :showId AND b.status = :status")
    int countByShowIdAndStatus(@Param("showId") Long showId, @Param("status") BookingStatus status);
}
