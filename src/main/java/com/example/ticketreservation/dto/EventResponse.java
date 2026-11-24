package com.example.ticketreservation.dto; 
import java.time.LocalDateTime; 
public record EventResponse(
    String id,
    String name,
    String venue,
    LocalDateTime date,
    int totalSeats,
    int availableSeats,
    double ticketPrice,
    String organizerId
){}
