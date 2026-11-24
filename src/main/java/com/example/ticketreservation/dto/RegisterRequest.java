package com.example.ticketreservation.dto; 
import jakarta.validation.constraints.NotBlank; 
import jakarta.validation.constraints.Size; 
public record RegisterRequest(
    @NotBlank @Size(min=3,max=40) String username,
    @NotBlank @Size(min=8) String password,
    @NotBlank @Size(max=100) String fullName
){}
