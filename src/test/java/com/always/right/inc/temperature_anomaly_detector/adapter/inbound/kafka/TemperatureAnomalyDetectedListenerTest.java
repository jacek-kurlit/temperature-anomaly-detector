package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import com.always.right.inc.temperature_anomaly_detector.KafkaBaseTest;
import com.always.right.inc.temperature_anomaly_detector.adapter.outbound.mongo.TemperatureAnomalyMongoRepository;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyDetectedEvent;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

class TemperatureAnomalyDetectedListenerTest extends KafkaBaseTest implements WithAssertions {

    @Autowired
    TemperatureAnomalyMongoRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldPersistAnomalyEvent() {
        // given
        var event = anomalyEvent(UUID.randomUUID());

        // when
        send(event);

        // then
        await().atMost(5, SECONDS).untilAsserted(() -> {
            var saved = repository.findById(event.anomalyId());
            assertThat(saved).isPresent();
            assertThat(saved.get().roomId().value()).isEqualTo(event.roomId());
            assertThat(saved.get().thermometerId().value()).isEqualTo(event.thermometerId());
            assertThat(saved.get().averageTemp()).isEqualTo(event.averageTemp());
            assertThat(saved.get().currentTemp()).isEqualTo(event.currentTemp());
            assertThat(saved.get().createdAt()).isEqualTo(event.timestamp());
        });
    }

    @Test
    void shouldSkipAlreadyPersistedAnomaly() {
        // given
        var event = anomalyEvent(UUID.randomUUID());

        // when
        send(event);
        send(event);

        // then: only one document despite two messages
        await().atMost(5, SECONDS).untilAsserted(() ->
                assertThat(repository.count()).isEqualTo(1)
        );
    }

    private void send(TemperatureAnomalyDetectedEvent event) {
        kafkaTemplate.send(
                MessageBuilder.withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, "temperature-anomalies")
                        .setHeader(KafkaHeaders.KEY, event.thermometerId())
                        .setHeader("__TypeId__", "temperature-anomaly-detected")
                        .build()
        );
    }

    private static TemperatureAnomalyDetectedEvent anomalyEvent(UUID id) {
        return new TemperatureAnomalyDetectedEvent(
                id,
                "room1",
                "thermo-001",
                20.0,
                27.5,
                Instant.parse("2025-01-15T10:30:00Z")
        );
    }
}
