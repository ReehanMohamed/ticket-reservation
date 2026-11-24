package com.example.ticketreservation.service;
import com.example.ticketreservation.dto.StatsResponse;
import com.example.ticketreservation.model.*;
import com.example.ticketreservation.repository.*;
import org.springframework.stereotype.Service;

@Service public class AdminService{
    private final UserRepository users;
    private final EventRepository events;
    private final ReservationRepository reservations;
    public AdminService(
        UserRepository u,
        EventRepository e,
        ReservationRepository r
    )

    {
        users=u;
        events=e;
        reservations=r;
    }
    
    public StatsResponse stats(){
        var confirmed=reservations.findByStatus(
            ReservationStatus.CONFIRMED
        );
        
        long tickets=confirmed.stream().mapToLong(Reservation::getSeats).sum();
        double revenue=confirmed.stream().mapToDouble(Reservation::getAmount).sum();
        return new StatsResponse(users.count(),events.count(),tickets,revenue);
    }
}
