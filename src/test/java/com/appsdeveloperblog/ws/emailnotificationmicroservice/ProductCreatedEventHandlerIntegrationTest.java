package com.appsdeveloperblog.ws.emailnotificationmicroservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.appsdeveloperblog.ws.coreblog.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.handler.ProductCreatedEventHandler;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.model.ProcessedEventEntity;
import com.appsdeveloperblog.ws.emailnotificationmicroservice.repository.ProcessedEventRepository;

@ActiveProfiles("integration-test") // application-test.properties
@EmbeddedKafka
@SpringBootTest(properties="spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductCreatedEventHandlerIntegrationTest {

	@SuppressWarnings("removal")
	@MockBean
	ProcessedEventRepository processedEventRepository;

	@SuppressWarnings("removal")
	@MockBean
	RestTemplate restTemplate;

	@SuppressWarnings("removal")
	@SpyBean
	ProductCreatedEventHandler productCreatedEventHandler;

	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;

	@Test
	public void testProductCreatedEventHandler_OnProductCreated_HandlesEvent() throws Exception{

		// Arrange
		ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
		productCreatedEvent.setPrice(new BigDecimal(10));
		productCreatedEvent.setProductId(UUID.randomUUID());
		productCreatedEvent.setQuantity(1);
		productCreatedEvent.setTitle("Test product");

		String messageId = UUID.randomUUID().toString();
		String messageKey = productCreatedEvent.getProductId().toString();

		ProducerRecord<String, Object> record = new ProducerRecord<>("product-created-events-topic", messageKey, productCreatedEvent);
		record.headers().add("messageId", messageId.getBytes());
		record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

		ProcessedEventEntity processedEventEntity = new ProcessedEventEntity();
		when(processedEventRepository.findByMessageId(anyString())).thenReturn(Optional.of(processedEventEntity));
		when(processedEventRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);

		String responseBody = "{\"key\":\"value\"}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers, HttpStatus.OK);

		when(restTemplate.exchange(
				any(String.class),
				any(HttpMethod.class),
				isNull(), eq(String.class)
		))
				.thenReturn(responseEntity);

		// Act
		kafkaTemplate.send(record).get();

		// Assert
		ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);

		verify(productCreatedEventHandler, timeout(10000).times(1)).handle(eventCaptor.capture(),
				messageIdCaptor.capture(),
				messageKeyCaptor.capture());

		assertEquals(messageId, messageIdCaptor.getValue());
		assertEquals(messageKey, messageKeyCaptor.getValue());
		assertEquals(productCreatedEvent.getProductId(), eventCaptor.getValue().getProductId());

	}

}
