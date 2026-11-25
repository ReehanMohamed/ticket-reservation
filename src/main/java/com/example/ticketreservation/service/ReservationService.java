package com.example.ticketreservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.ticketreservation.dto.ReservationRequest;
import com.example.ticketreservation.dto.ReservationResponse;
import com.example.ticketreservation.dto.ReservationUpdateRequest;
import com.example.ticketreservation.exception.ApiException;
import com.example.ticketreservation.model.Event;
import com.example.ticketreservation.model.Reservation;
import com.example.ticketreservation.model.ReservationStatus;
import com.example.ticketreservation.repository.ReservationRepository;
import com.example.ticketreservation.repository.UserRepository;

@Service
public class ReservationService {

    private final ReservationRepository repo;
    private final EventService events;
    private final UserRepository users;

    public ReservationService(
        ReservationRepository r,
        EventService e,
        UserRepository u
    ) {
        repo = r;
        events = e;
        users = u;
    }

    public ReservationResponse book(
        ReservationRequest q,
        String customer
    ) {
        Event e = events.takeSeats(q.eventId(), q.seats());

        Reservation r = Reservation.builder()
            .code("TR-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase())
            .customerId(customer)
            .eventId(e.getId())
            .seats(q.seats())
            .amount(q.seats() * e.getTicketPrice())
            .status(ReservationStatus.CONFIRMED)
            .createdAt(LocalDateTime.now())
            .build();

        try {
            return dto(repo.save(r));
        } catch (Exception ex) {
            events.releaseSeats(e.getId(), q.seats());
            throw ex;
        }
    }

    public List<ReservationResponse> mine(String username) {
        return repo.findByCustomerId(username)
            .stream()
            .map(this::dto)
            .toList();
    }

    public List<ReservationResponse> all() {
        return repo.findAll()
            .stream()
            .map(this::dto)
            .toList();
    }

    public ReservationResponse update(
        String id,
        ReservationUpdateRequest q,
        String username
    ) {
        Reservation r = repo.findById(id)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Reservation not found"
            ));

        if (!r.getCustomerId().equals(username)) {
            throw new ApiException(
                HttpStatus.FORBIDDEN,
                "You can only update your own reservation"
            );
        }

        if (r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ApiException(
                HttpStatus.CONFLICT,
                "Only confirmed reservations can be updated"
            );
        }

        int oldSeats = r.getSeats();
        int newSeats = q.seats();
        int difference = newSeats - oldSeats;

        if (difference == 0) {
            return dto(r);
        }

        double oldPricePerSeat = r.getAmount() / oldSeats;

        // Reserve additional seats or release unused seats.
        if (difference > 0) {
            events.takeSeats(r.getEventId(), difference);
        } else {
            events.releaseSeats(r.getEventId(), -difference);
        }

        r.setSeats(newSeats);
        r.setAmount(oldPricePerSeat * newSeats);

        try {
            return dto(repo.save(r));
        } catch (Exception ex) {
            // Restore the event's seat count if saving fails.
            if (difference > 0) {
                events.releaseSeats(r.getEventId(), difference);
            } else {
                events.takeSeats(r.getEventId(), -difference);
            }

            throw ex;
        }
    }

    public void cancel(
        String id,
        String username,
        boolean admin
    ) {
        Reservation r = repo.findById(id)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Reservation not found"
            ));

        if (!admin && !r.getCustomerId().equals(username)) {
            throw new ApiException(
                HttpStatus.FORBIDDEN,
                "You can only cancel your own reservation"
            );
        }

        if (r.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException(
                HttpStatus.CONFLICT,
                "Reservation already cancelled"
            );
        }

        r.setStatus(ReservationStatus.CANCELLED);
        repo.save(r);

        events.releaseSeats(r.getEventId(), r.getSeats());
    }

    public ReservationResponse dto(Reservation r) {
        return new ReservationResponse(
            r.getId(),
            r.getCode(),
            r.getCustomerId(),
            r.getEventId(),
            r.getSeats(),
            r.getAmount(),
            r.getStatus(),
            r.getCreatedAt()
        );
    }
}