package com.moviebooking.repository;

import com.moviebooking.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    
    List<Theatre> findByActiveTrue();
    
    List<Theatre> findByCityIdAndActiveTrue(Long cityId);
    
    @Query("SELECT t FROM Theatre t WHERE t.city.name = :cityName AND t.active = true")
    List<Theatre> findByCityNameAndActiveTrue(@Param("cityName") String cityName);
    
    @Query("SELECT DISTINCT t FROM Theatre t " +
           "JOIN t.shows s " +
           "WHERE t.city.id = :cityId AND s.movie.id = :movieId AND t.active = true")
    List<Theatre> findTheatresRunningMovie(@Param("cityId") Long cityId, 
                                            @Param("movieId") Long movieId);
}
