package com.example.ticketreservation.dto; 
import com.example.ticketreservation.model.ReservationStatus; 
import java.time.LocalDateTime; 
public record ReservationResponse(
    String id,
    String code,
    String customerId,
    String eventId,
    int seats,
    double amount,
    ReservationStatus status,
    LocalDateTime createdAt
){}
