package com.always.right.inc.temperature_anomaly_detector.service;

import com.always.right.inc.temperature_anomaly_detector.domain.AnomalyDetectionAlgorithm;
import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureMeasurement;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class TemperatureAnomalyDetectionService {
    private final ConcurrentMap<ThermometerId, AnomalyDetectionAlgorithm> algorithms;
    private final EventPublisher eventPublisher;
    private final AnomalyDetectionAlgorithmFactory algorithmFactory;

    public TemperatureAnomalyDetectionService(EventPublisher eventPublisher, AnomalyDetectionAlgorithmFactory algorithmFactory) {
        this.eventPublisher = eventPublisher;
        this.algorithmFactory = algorithmFactory;
        this.algorithms = new ConcurrentHashMap<>();
    }

    public void handle(RoomId roomId,
                       ThermometerId thermometerId,
                       TemperatureMeasurement measurement) {
        //Important: Assumption is that events are partitioned by thermometerId otherwise this will not work!
        AnomalyDetectionAlgorithm anomalyDetectionAlgorithm = algorithms
                .computeIfAbsent(thermometerId, key -> algorithmFactory.create());
        anomalyDetectionAlgorithm
                .accept(measurement)
                .map(anomaly -> new TemperatureAnomalyDetectedEvent(
                        UUID.randomUUID(),
                        roomId.value(),
                        thermometerId.value(),
                        anomaly.averageTemperature(),
                        measurement.value(),
                        Instant.now()
                ))
                .ifPresent(eventPublisher::publish);

    }

}
