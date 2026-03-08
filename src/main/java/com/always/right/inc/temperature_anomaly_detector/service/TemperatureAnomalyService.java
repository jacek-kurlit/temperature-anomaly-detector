package com.always.right.inc.temperature_anomaly_detector.service;

import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomalyRepository;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class TemperatureAnomalyService {

    private final TemperatureAnomalyRepository repository;

    public void handle(UUID anomalyId,
                       RoomId roomId,
                       ThermometerId thermometerId,
                       double averageTemp,
                       double currentTemp,
                       Instant timestamp) {

        if (repository.existsById(anomalyId)) {
            log.warn("Anomaly {} already persisted, skipping", anomalyId);
            return;
        }
        repository.save(new TemperatureAnomaly(
                anomalyId,
                roomId,
                thermometerId,
                averageTemp,
                currentTemp,
                timestamp
        ));

    }
}
