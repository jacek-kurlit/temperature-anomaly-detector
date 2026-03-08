package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import com.always.right.inc.temperature_anomaly_detector.KafkaBaseTest;
import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureMeasurement;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyDetectionService;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class TemperatureMeasurementsListenerTest extends KafkaBaseTest implements WithAssertions {

    @MockitoBean
    TemperatureAnomalyDetectionService service;

    @Test
    void shouldDeserializeEventAndDelegateToService() {
        // given
        var event = new TemperatureMeasurementEvent(
                "thermo-001",
                "room1",
                23.5,
                Instant.parse("2025-01-15T10:30:00Z")
        );

        // when
        send(event);

        // then
        var measurementCaptor = ArgumentCaptor.forClass(TemperatureMeasurement.class);
        await().atMost(5, SECONDS)
                .untilAsserted(() -> {
                            verify(service).handle(
                                    eq(new RoomId(event.roomId())),
                                    eq(new ThermometerId(event.thermometerId())),
                                    measurementCaptor.capture()
                            );
                            assertThat(measurementCaptor.getValue().value()).isEqualTo(event.temperature());
                            assertThat(measurementCaptor.getValue().timestamp()).isEqualTo(event.timestamp());
                        }
                );
    }

    private void send(TemperatureMeasurementEvent event) {
        kafkaTemplate.send(
                MessageBuilder.withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, "temperature-measurements")
                        .setHeader(KafkaHeaders.KEY, event.thermometerId())
                        .setHeader("__TypeId__", "temperature-measurement")
                        .build()
        );
    }
}
