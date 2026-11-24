package com.example.ticketreservation.service;
import com.example.ticketreservation.dto.*;
import com.example.ticketreservation.model.*;
import com.example.ticketreservation.repository.*;
import com.example.ticketreservation.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.*;
import java.util.*;

@Service 

public class EventService{
    private final EventRepository repo;
    private final ReservationRepository reservations;
    private final MongoTemplate mongo;
    public EventService(EventRepository r,ReservationRepository b,MongoTemplate m){
        repo=r;
        reservations=b;
        mongo=m;
    }
    public List<EventResponse> all(String name){
        return (name==null||name.isBlank()?repo.findAll():repo.findByNameContainingIgnoreCase(name)).stream().map(this::dto).toList();
    }
    public EventResponse get(String id){
        return dto(find(id));
    }
    public Event find(String id){
        return repo.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Event not found"));
    }
    public EventResponse create(EventRequest q,String organizer){
        Event e=Event.builder().name(q.name()).venue(q.venue()).date(q.eventDate()).totalSeats(q.totalSeats()).availableSeats(q.totalSeats()).ticketPrice(q.ticketPrice()).organizerId(organizer).build();
        return dto(repo.save(e));
    }
    public EventResponse update(String id,EventRequest q,String username,boolean admin){
        Event e=find(id);
        if(!admin&&!e.getOrganizerId().equals(username))
            throw new ApiException(HttpStatus.FORBIDDEN,"You can only update your own events");
        int reserved=e.getTotalSeats()-e.getAvailableSeats();
        if(q.totalSeats()<reserved)
            throw new ApiException(HttpStatus.CONFLICT,"Total seats cannot be lower than seats already reserved");
        e.setName(q.name());
        e.setVenue(q.venue());
        e.setDate(q.eventDate());
        e.setTotalSeats(q.totalSeats());
        e.setAvailableSeats(q.totalSeats()-reserved);
        e.setTicketPrice(q.ticketPrice());
        return dto(repo.save(e));
    }
    public void delete(String id){
        find(id);
        if(reservations.existsByEventIdAndStatus(id,ReservationStatus.CONFIRMED))
            throw new ApiException(HttpStatus.CONFLICT,"Event has confirmed reservations");
        repo.deleteById(id);
    }
    public EventResponse dto(Event e){
        return new EventResponse(e.getId(),e.getName(),e.getVenue(),e.getDate(),e.getTotalSeats(),e.getAvailableSeats(),e.getTicketPrice(),e.getOrganizerId());
    }
    public Event takeSeats(String id,int seats){
        Query q=new Query(Criteria.where("_id").is(id).and("availableSeats").gte(seats));
        Update u=new Update().inc("availableSeats",-seats);
        Event e=mongo.findAndModify(q,u,FindAndModifyOptions.options().returnNew(true),Event.class);
        if(e==null)
            throw new ApiException(HttpStatus.CONFLICT,"Not enough seats available");
        return e;
    }
    public void releaseSeats(String id,int seats){
        mongo.updateFirst(new Query(Criteria.where("_id").is(id)),new Update().inc("availableSeats",seats),Event.class);
    }
}