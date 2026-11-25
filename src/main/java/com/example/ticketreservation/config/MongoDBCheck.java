
package com.example.ticketreservation.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoDBCheck implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;
    private final Environment environment;

     public MongoDBCheck(
            MongoTemplate mongoTemplate,
            Environment environment) {
        this.mongoTemplate = mongoTemplate;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {

       System.out.println("\n========== MONGODB DIAGNOSTICS ==========");

System.out.println("Connected Database: "
        + mongoTemplate.getDb().getName());

System.out.println("Configured MongoDB URI: "
        + environment.getProperty("spring.mongodb.uri"));

System.out.println("Configured Database Property: "
        + environment.getProperty("spring.data.mongodb.database"));

System.out.println("Active Profiles: "
        + String.join(", ", environment.getActiveProfiles()));

System.out.println("=========================================\n");
    }
}
