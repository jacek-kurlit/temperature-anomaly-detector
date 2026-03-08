package com.always.right.inc.temperature_anomaly_detector.service;

import com.always.right.inc.temperature_anomaly_detector.adapter.config.DetectionProperties;
import com.always.right.inc.temperature_anomaly_detector.domain.AnomalyDetectionAlgorithm;
import com.always.right.inc.temperature_anomaly_detector.domain.ConsecutiveDetectionAlgorithm;
import com.always.right.inc.temperature_anomaly_detector.domain.TimeWindowDetectionAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnomalyDetectionAlgorithmFactory {
    private final DetectionProperties detectionProperties;

    public AnomalyDetectionAlgorithm create() {
        return switch (detectionProperties.getType()) {
            case CONSECUTIVE -> new ConsecutiveDetectionAlgorithm(detectionProperties.getWindowSize(), detectionProperties.getThreshold());
            case TIME_WINDOW -> new TimeWindowDetectionAlgorithm();
        };
    }
}
