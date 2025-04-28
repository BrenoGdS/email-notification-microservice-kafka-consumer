package com.appsdeveloperblog.ws.emailnotificationmicroservice.handler;

import com.appsdeveloperblog.ws.coreblog.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.exception.NonRetryableException;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.exception.RetryableException;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.model.ProcessedEventEntity;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.repository.ProcessedEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FrameworkServlet;

import java.net.SocketTimeoutException;
import java.util.Optional;

@Component
@KafkaListener(topics="product-created-events-topic") //It can be configured to listen to multiple topics: e.g.: @KafkaListener(topics = {"topic|", "topic2"})
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    ProcessedEventRepository processedEventRepository;

    public ProductCreatedEventHandler(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaHandler
    @Transactional
    public void handle(@Payload ProductCreatedEvent event,
                       @Header("messageId") String messageId,
                       @Header("kafka_receivedMessageKey") String messageKey) {
        try {
            Optional<ProcessedEventEntity> existing = processedEventRepository.findByMessageId(messageId);
            if (existing.isPresent()) {
                LOGGER.info("Duplicate message received (ID: {}), skipping processing.", messageId);
                return;
            }
        } catch (RetryableException ex) {
            throw new RetryableException("Remote service timed out, retrying...", ex);
        } catch (NullPointerException ex) {
            throw new NonRetryableException("Non-recoverable error occurred", ex);
        } catch (Exception ex) {
            throw new NonRetryableException("Non-recoverable error occurred", ex);
        }
        // Save a unique message id in a database table
        try {
            LOGGER.info("Received a new event: " + event.toString());
            processedEventRepository.save(new ProcessedEventEntity(messageId, event.getProductId().toString()));
        } catch (DataIntegrityViolationException ex) {
            throw new NonRetryableException(ex);
        }
    }
}
