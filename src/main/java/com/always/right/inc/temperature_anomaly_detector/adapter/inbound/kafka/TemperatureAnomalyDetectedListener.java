package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomalyRepository;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyDetectedEvent;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureAnomalyDetectedListener {

    private final TemperatureAnomalyService temperatureAnomalyService;

    @KafkaListener(topics = "temperature-anomalies")
    public void handle(TemperatureAnomalyDetectedEvent event) {
        log.info("Handling anomaly detection {}", event.anomalyId());
        temperatureAnomalyService.handle(
                event.anomalyId(),
                new RoomId(event.roomId()),
                new ThermometerId(event.thermometerId()),
                event.averageTemp(),
                event.currentTemp(),
                event.timestamp()
        );
        log.info("Persisted anomaly {} for thermometer {}", event.anomalyId(), event.thermometerId());
    }
}
