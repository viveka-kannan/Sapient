package com.moviebooking.repository;

import com.moviebooking.entity.ShowSeat;
import com.moviebooking.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    
    List<ShowSeat> findByShowIdOrderBySeatRowNumberAscSeatSeatNumberAsc(Long showId);
    
    List<ShowSeat> findByShowIdAndStatus(Long showId, SeatStatus status);
    
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id IN :seatIds")
    List<ShowSeat> findByShowIdAndSeatIds(@Param("showId") Long showId, 
                                           @Param("seatIds") List<Long> seatIds);
    
    /**
     * Find seats with pessimistic lock for booking
     * This prevents race conditions during concurrent bookings
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id IN :seatIds AND ss.status = 'AVAILABLE'")
    List<ShowSeat> findAvailableSeatsForBooking(@Param("showId") Long showId, 
                                                 @Param("seatIds") List<Long> seatIds);
    
    @Query("SELECT COUNT(ss) FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = :status")
    int countByShowIdAndStatus(@Param("showId") Long showId, @Param("status") SeatStatus status);
    
    @Query("SELECT MIN(ss.price) FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = 'AVAILABLE'")
    Optional<Double> findMinPriceByShowId(@Param("showId") Long showId);
}
