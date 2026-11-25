
package com.example.ticketreservation.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoCollectionInitializer {

    @Bean
    public ApplicationRunner initializeCollections(
            MongoTemplate mongoTemplate) {

        return args -> {
            createCollectionIfMissing(mongoTemplate, "users");
            createCollectionIfMissing(mongoTemplate, "events");
            createCollectionIfMissing(mongoTemplate, "reservations");
        };
    }

    private void createCollectionIfMissing(
            MongoTemplate mongoTemplate, String collectionName) {

        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
            System.out.println(
                    "Created MongoDB collection: " + collectionName);
        } else {
            System.out.println(
                    "MongoDB collection already exists: " + collectionName);
        }
    }
}
