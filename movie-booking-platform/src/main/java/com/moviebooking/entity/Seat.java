package com.moviebooking.entity;

import com.moviebooking.enums.SeatCategory;
import com.moviebooking.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Seat entity - represents a seat in a screen
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"screen_id", "row_number", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {

    @Column(name = "row_number", nullable = false)
    private String rowNumber; // A, B, C, etc.

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber; // 1, 2, 3, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatCategory category; // REGULAR, PREMIUM, VIP

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    /**
     * Returns the seat identifier (e.g., "A-1", "B-5")
     */
    public String getSeatIdentifier() {
        return rowNumber + "-" + seatNumber;
    }
}
