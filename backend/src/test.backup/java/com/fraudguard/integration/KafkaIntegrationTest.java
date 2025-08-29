package com.fraudguard.integration;

import com.fraudguard.dto.TransactionEvent;
import com.fraudguard.service.messaging.TransactionEventProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = {"transaction-events", "fraud-alerts"})
@DirtiesContext
class KafkaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionEventProducer transactionEventProducer;

    private CountDownLatch latch = new CountDownLatch(1);
    private TransactionEvent receivedEvent;

    @Test
    void shouldSendAndReceiveTransactionEvent() throws InterruptedException {
        // Given
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .merchantId("kafka-test-merchant")
                .fraudScore(new BigDecimal("0.4"))
                .riskLevel("MEDIUM")
                .status("PENDING")
                .build();

        // When
        transactionEventProducer.sendTransactionEvent(event);

        // Then
        boolean messageReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(messageReceived).isTrue();
        assertThat(receivedEvent).isNotNull();
        assertThat(receivedEvent.getTransactionId()).isEqualTo(event.getTransactionId());
        assertThat(receivedEvent.getAmount()).isEqualTo(new BigDecimal("250.00"));
        assertThat(receivedEvent.getFraudScore()).isEqualTo(new BigDecimal("0.4"));
    }

    @KafkaListener(topics = "transaction-events", groupId = "test-group")
    public void receiveTransactionEvent(ConsumerRecord<String, TransactionEvent> record) {
        receivedEvent = record.value();
        latch.countDown();
    }
}
