package com.example.ticketreservation.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ticketreservation.dto.AuthResponse;
import com.example.ticketreservation.dto.LoginRequest;
import com.example.ticketreservation.dto.RegisterRequest;
import com.example.ticketreservation.dto.UserResponse;
import com.example.ticketreservation.dto.UserUpdateRequest;
import com.example.ticketreservation.exception.ApiException;
import com.example.ticketreservation.model.AppUser;
import com.example.ticketreservation.model.ReservationStatus;
import com.example.ticketreservation.model.Role;
import com.example.ticketreservation.repository.ReservationRepository;
import com.example.ticketreservation.repository.UserRepository;
import com.example.ticketreservation.security.JwtService;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;
    private final JwtService jwt;
    private final ReservationRepository reservations;

    public AuthService(
        UserRepository r,
        PasswordEncoder e,
        AuthenticationManager m,
        JwtService j,
        ReservationRepository reservations
    ) {
        repo = r;
        encoder = e;
        manager = m;
        jwt = j;
        this.reservations = reservations;
    }

    public AuthResponse register(RegisterRequest q) {
        if (repo.existsByUsername(q.username())) {
            throw new ApiException(
                HttpStatus.CONFLICT,
                "Username already exists"
            );
        }

        AppUser u = repo.save(
            AppUser.builder()
                .username(q.username())
                .password(encoder.encode(q.password()))
                .fullName(q.fullName())
                .role(Role.CUSTOMER)
                .build()
        );

        return auth(u);
    }

    public AuthResponse login(LoginRequest q) {
        try {
            manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    q.username(),
                    q.password()
                )
            );
        } catch (Exception e) {
            throw new ApiException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
            );
        }

        return auth(
            repo.findByUsername(q.username()).orElseThrow()
        );
    }

    private AuthResponse auth(AppUser u) {
        return new AuthResponse(
            jwt.generate(u.getUsername()),
            "Bearer",
            jwt.getExpiration(),
            response(u)
        );
    }

    public UserResponse me(String username) {
        return response(
            repo.findByUsername(username).orElseThrow()
        );
    }

    public List<UserResponse> users() {
        return repo.findAll()
            .stream()
            .map(this::response)
            .toList();
    }

    public UserResponse updateUser(
        String id,
        UserUpdateRequest q
    ) {
        AppUser u = repo.findById(id)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "User not found"
            ));

        u.setFullName(q.fullName());

        if (q.role() != null) {
            u.setRole(q.role());
        }

        return response(repo.save(u));
    }

    public void deleteUser(
        String id,
        String requesterUsername
    ) {
        AppUser u = repo.findById(id)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "User not found"
            ));

        if (u.getUsername().equals(requesterUsername)) {
            throw new ApiException(
                HttpStatus.CONFLICT,
                "You cannot delete your own admin account"
            );
        }

        boolean hasConfirmedReservations =
            reservations.existsByCustomerIdAndStatus(
                u.getUsername(),
                ReservationStatus.CONFIRMED
            );

        if (hasConfirmedReservations) {
            throw new ApiException(
                HttpStatus.CONFLICT,
                "Cannot delete user because they have confirmed reservations"
            );
        }

        repo.delete(u);
    }

    public UserResponse response(AppUser u) {
        return new UserResponse(
            u.getId(),
            u.getUsername(),
            u.getFullName(),
            u.getRole()
        );
    }
}