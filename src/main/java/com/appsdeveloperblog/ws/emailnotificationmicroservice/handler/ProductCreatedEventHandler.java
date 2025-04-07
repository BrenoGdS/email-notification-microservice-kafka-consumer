package com.appsdeveloperblog.ws.emailnotificationmicroservice.handler;

import com.appsdeveloperblog.ws.coreblog.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.exception.NonRetryableException;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.exception.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FrameworkServlet;

import java.net.SocketTimeoutException;

@Component
@KafkaListener(topics="product-created-events-topic") //It can be configured to listen to multiple topics: e.g.: @KafkaListener(topics = {"topic|", "topic2"})
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final FrameworkServlet frameworkServlet;

    public ProductCreatedEventHandler(FrameworkServlet frameworkServlet) {
        this.frameworkServlet = frameworkServlet;
    }

    @KafkaHandler
    public void handle(ProductCreatedEvent productCreatedEvent) {
        try {
            // Process the event (for instance, call a remote service)
            LOGGER.info("Received a new event: " + productCreatedEvent.toString());

        } catch (RetryableException ex) { //SomeTransientException
            // Transient error – retry might succeed
            throw new RetryableException("Remote service timed out, retrying...", ex);
        } catch (NullPointerException ex) {
            // Non-recoverable error – do not retry
            throw new NonRetryableException("Non-recoverable error occurred", ex);
        } catch (Exception ex) {
            // Non-recoverable error – do not retry
            throw new NonRetryableException("Non-recoverable error occurred", ex);
        }
    }
}
