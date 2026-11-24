package com.example.ticketreservation.dto; 
public record AuthResponse(
    String token,
    String type,
    long expiresIn,
    UserResponse user
){}
