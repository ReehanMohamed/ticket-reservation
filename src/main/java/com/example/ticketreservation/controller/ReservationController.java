package com.example.ticketreservation.controller;

import com.example.ticketreservation.dto.*;
import com.example.ticketreservation.service.ReservationService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService s;

    public ReservationController(ReservationService s) {
        this.s = s;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReservationResponse book(
        @Valid @RequestBody ReservationRequest q,
        Authentication a
    ) {
        return s.book(q, a.getName());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<ReservationResponse> mine(Authentication a) {
        return s.mine(a.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<ReservationResponse> all() {
        return s.all();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReservationResponse update(
        @PathVariable String id,
        @Valid @RequestBody ReservationUpdateRequest q,
        Authentication a
    ) {
        return s.update(id, q, a.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public void cancel(
        @PathVariable String id,
        Authentication a
    ) {
        boolean admin = a.getAuthorities().stream()
            .anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"));

        s.cancel(id, a.getName(), admin);
    }
}