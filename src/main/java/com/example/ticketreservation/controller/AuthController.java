package com.example.ticketreservation.controller;
import com.example.ticketreservation.dto.*;
import com.example.ticketreservation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController 

@RequestMapping("/api/auth") 

public class AuthController{
    private final AuthService s;
    public AuthController(AuthService s){
        this.s=s;
    }
@PostMapping("/register")

public AuthResponse register(
    @Valid 
    @RequestBody RegisterRequest q){
        return s.register(q);
    }
    
@PostMapping("/login")
public AuthResponse login(
    @Valid 
    @RequestBody LoginRequest q)
    {
        return s.login(q);
    }
    @GetMapping("/me")
    public UserResponse me(Authentication a)
    {
        return s.me(a.getName());
    }
}
