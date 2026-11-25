package com.example.ticketreservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReservationUpdateRequest(
    @Min(1)
    @Max(10)
    int seats
) {}