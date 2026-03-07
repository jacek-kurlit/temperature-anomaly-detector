package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class TemperatureMeasurementsListener {

    @KafkaListener(topics = "temperature-measurements")
    public void handleTemperatureMeasurement(TemperatureMeasurementEvent event) {
        log.info("It's working! {}", event);
    }

    public record TemperatureMeasurementEvent(
            String thermometerId,
            String roomId,
            Double temperature,
            Instant timestamp
    ) {
    }
}
