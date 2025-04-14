package com.appsdeveloperblog.ws.emailnotificationmicroservice.repository;

import com.appsdeveloperblog.ws.emailnotificationmicroservice.model.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, Long> {
    Optional<ProcessedEventEntity> findByMessageId(String messageId);
}