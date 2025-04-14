package com.appsdeveloperblog.ws.emailnotificationmicroservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
@Data
@Entity
@Table(name = "processedEvents")
public class ProcessedEventEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false, unique = true)
    private String messageId; // Unique Kafka message ID

    @NonNull
    @Column(nullable = false)
    private String productId;

    public ProcessedEventEntity() {
    }
}
