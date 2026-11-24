package com.example.ticketreservation.dto; 
import java.time.LocalDateTime;
 
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record EventRequest(
    @NotBlank String name,
    @NotBlank String venue,
    @NotBlank String description,
    @NotNull @Future LocalDateTime eventDate,
    @Min(1) int totalSeats,
    @DecimalMin("0.0") double ticketPrice
){}
