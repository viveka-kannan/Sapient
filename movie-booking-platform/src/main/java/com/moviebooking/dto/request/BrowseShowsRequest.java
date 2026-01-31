package com.moviebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * Request DTO for browsing theatres running a movie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrowseShowsRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotBlank(message = "City name is required")
    private String cityName;

    @NotNull(message = "Date is required")
    private LocalDate date;
}
