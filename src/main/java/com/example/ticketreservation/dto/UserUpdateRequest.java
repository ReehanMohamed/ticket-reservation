package com.example.ticketreservation.dto;

import com.example.ticketreservation.model.Role;


import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
    @NotBlank String fullName,
    Role role
) {}