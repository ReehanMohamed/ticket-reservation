package com.example.ticketreservation.controller;
import com.example.ticketreservation.dto.*;
import com.example.ticketreservation.service.EventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController 
@RequestMapping("/api/events")

public class EventController{
    private final EventService s;
    public EventController(EventService s){
        this.s=s;
    }

@GetMapping public List<EventResponse> all
(@RequestParam(required=false)String name){
    return s.all(name);
}

@GetMapping("/{id}")

public EventResponse get(
    @PathVariable String id
){
    return s.get(id);
}

@PostMapping 
@PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")

public EventResponse create(
    @Valid 
    @RequestBody EventRequest q,Authentication a
){
    return s.create(q,a.getName());
}

@PutMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")

public EventResponse update

(@PathVariable String id,
    @Valid 
    @RequestBody EventRequest q,
    Authentication a
)

{return s.update(
    id,
    q,
    a.getName(),
    a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")));

}

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")

public void delete(
    @PathVariable String id
){
    s.delete(id);
}

}
