package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureMeasurement;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemperatureMeasurementsListener {

    private final TemperatureAnomalyDetectionService temperatureAnomalyDetectionService;

    @KafkaListener(topics = "temperature-measurements")
    public void handleTemperatureMeasurement(TemperatureMeasurementEvent event) {
        log.trace("Received temp measurement event for roomId: {}, thermometerId: {}", event.roomId(), event.thermometerId());
        //TODO: we are missing events deduplication here
        // kafka assures at least once delivery which means event can be resent and taken into
        // account once more
        temperatureAnomalyDetectionService.handle(
                new RoomId(event.roomId()),
                new ThermometerId(event.thermometerId()),
                new TemperatureMeasurement(event.temperature(), event.timestamp())
        );
        log.trace("Event for roomId: {}, thermometerId: {} handled", event.roomId(), event.thermometerId());
    }
}
