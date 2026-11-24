package com.example.ticketreservation.controller;
import com.example.ticketreservation.dto.*;
import com.example.ticketreservation.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController 
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")

public class AdminController{
    private final AuthService auth;
    private final AdminService admin;
    public AdminController(AuthService a,AdminService s)
    {auth=a;admin=s;}
@GetMapping("/users")
public java.util.List<UserResponse> 
users()
    {return 
        auth.users();
    }
@GetMapping("/stats")
public StatsResponse stats()
{
    return admin.stats();
}
}
