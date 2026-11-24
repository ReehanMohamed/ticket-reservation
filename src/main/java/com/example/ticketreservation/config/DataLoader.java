package com.example.ticketreservation.config;

import com.example.ticketreservation.model.*;
import com.example.ticketreservation.repository.*;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seed(
            UserRepository users,
            EventRepository events,
            PasswordEncoder encoder
    ) {
        return args -> {
            if (users.count() == 0) {
                users.save(AppUser.builder()
                        .username("admin")
                        .password(encoder.encode("Admin12345"))
                        .fullName("System Admin")
                        .role(Role.ADMIN)
                        .build());

                var organizer = users.save(AppUser.builder()
                        .username("organizer")
                        .password(encoder.encode("Organizer12345"))
                        .fullName("Demo Organizer")
                        .role(Role.ORGANIZER)
                        .build());

                users.save(AppUser.builder()
                        .username("customer")
                        .password(encoder.encode("Customer12345"))
                        .fullName("Demo Customer")
                        .role(Role.CUSTOMER)
                        .build());

                if (events.count() == 0) {
                    events.save(Event.builder()
                            .name("Tech Conference")
                            .venue("Main Auditorium")
                            .date(LocalDateTime.now().plusDays(30))
                            .totalSeats(100)
                            .availableSeats(100)
                            .ticketPrice(25)
                            .organizerId(organizer.getUsername())
                            .build());

                    events.save(Event.builder()
                            .name("Music Festival")
                            .venue("City Park")
                            .date(LocalDateTime.now().plusDays(45))
                            .totalSeats(200)
                            .availableSeats(200)
                            .ticketPrice(50)
                            .organizerId(organizer.getUsername())
                            .build());
                }
            }
        };
    }
}
