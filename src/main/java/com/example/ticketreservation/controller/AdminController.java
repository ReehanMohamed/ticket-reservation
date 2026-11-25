package com.example.ticketreservation.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketreservation.dto.StatsResponse;
import com.example.ticketreservation.dto.UserResponse;
import com.example.ticketreservation.dto.UserUpdateRequest;
import com.example.ticketreservation.service.AdminService;
import com.example.ticketreservation.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService auth;
    private final AdminService admin;

    public AdminController(
        AuthService a,
        AdminService s
    ) {
        auth = a;
        admin = s;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return auth.users();
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return admin.stats();
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(
        @PathVariable String id,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        return auth.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(
        @PathVariable String id,
        Authentication authentication
    ) {
        auth.deleteUser(id, authentication.getName());
    }
}