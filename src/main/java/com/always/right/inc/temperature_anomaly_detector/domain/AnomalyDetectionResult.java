package com.always.right.inc.temperature_anomaly_detector.domain;

public record AnomalyDetectionResult(
        double anomalyTemperature,
        double averageTemperature
) {
}
