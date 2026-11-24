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
@Document(collection="reservations") 

public class Reservation { 
    @Id private String id; 
    private String code; 
    private String customerId; 
    private String eventId; 
    private int seats; 
    private double amount; 
    private ReservationStatus status; 
    private LocalDateTime createdAt; 
}
