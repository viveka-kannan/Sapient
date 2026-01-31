package com.moviebooking.repository;

import com.moviebooking.entity.Show;
import com.moviebooking.enums.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    
    List<Show> findByTheatreIdAndShowDate(Long theatreId, LocalDate showDate);
    
    List<Show> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);
    
    /**
     * Find all shows for a movie in a specific city on a given date
     * This is the main query for the READ scenario
     */
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.theatre t " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH s.movie m " +
           "WHERE s.movie.id = :movieId " +
           "AND t.city.id = :cityId " +
           "AND s.showDate = :showDate " +
           "AND s.status IN :statuses " +
           "ORDER BY t.name, s.startTime")
    List<Show> findShowsByMovieAndCityAndDate(
            @Param("movieId") Long movieId,
            @Param("cityId") Long cityId,
            @Param("showDate") LocalDate showDate,
            @Param("statuses") List<ShowStatus> statuses);
    
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.theatre t " +
           "JOIN FETCH t.city c " +
           "WHERE c.name = :cityName " +
           "AND s.movie.id = :movieId " +
           "AND s.showDate = :showDate " +
           "AND s.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Show> findShowsByCityNameAndMovieAndDate(
            @Param("cityName") String cityName,
            @Param("movieId") Long movieId,
            @Param("showDate") LocalDate showDate);
}
