package com.appsdeveloperblog.ws.emailnotificationmicroservice.handler;

import com.appsdeveloperblog.ws.coreblog.event.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics="product-created-events-topic") //It can be configured to listen to multiple topics: e.g.: @KafkaListener(topics = {"topic|", "topic2"})
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handle(ProductCreatedEvent productCreatedEvent) {
        LOGGER.info("Received a new event: " + productCreatedEvent.toString());

    }
}
