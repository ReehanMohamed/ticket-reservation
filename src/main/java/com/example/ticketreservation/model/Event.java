package com.example.ticketreservation.model;
import java.time.LocalDateTime;

 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.Document;

 import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
@Document(collection="events") 
public class Event { 
    @Id 
    private String id; 
    private String name; 
    private String venue; 
    private LocalDateTime date; 
    private int totalSeats; 
    private int availableSeats; 
    private double ticketPrice; 
    private String organizerId; 
}
