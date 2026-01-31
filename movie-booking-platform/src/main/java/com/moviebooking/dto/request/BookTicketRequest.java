package com.moviebooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

/**
 * Request DTO for booking tickets
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookTicketRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    private String customerPhone;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(min = 1, max = 10, message = "You can book between 1 and 10 seats")
    private List<Long> seatIds;
}
