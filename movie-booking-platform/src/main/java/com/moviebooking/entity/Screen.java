package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Screen entity - represents a screen/auditorium within a theatre
 */
@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen extends BaseEntity {

    @Column(nullable = false)
    private String name; // Screen 1, Screen 2, IMAX, etc.

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "screen_type")
    private String screenType; // REGULAR, IMAX, 3D, 4DX

    @Column(name = "is_active")
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Seat> seats = new HashSet<>();

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Show> shows = new HashSet<>();
}
