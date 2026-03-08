package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomalyRepository;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureAnomalyDetectedListener {

    private final TemperatureAnomalyRepository repository;

    @KafkaListener(topics = "temperature-anomalies")
    public void handle(TemperatureAnomalyDetectedEvent event) {
        log.info("Handling anomaly detection {}", event.anomalyId());
        if (repository.existsById(event.anomalyId())) {
            log.warn("Anomaly {} already persisted, skipping", event.anomalyId());
            return;
        }

        repository.save(new TemperatureAnomaly(
                event.anomalyId(),
                event.roomId().value(),
                event.thermometerId().value(),
                event.averageTemp(),
                event.currentTemp(),
                event.timestamp()
        ));
        log.info("Persisted anomaly {} for thermometer {}", event.anomalyId(), event.thermometerId());
    }
}
