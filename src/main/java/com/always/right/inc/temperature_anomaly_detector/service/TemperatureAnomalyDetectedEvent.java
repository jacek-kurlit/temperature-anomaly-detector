package com.always.right.inc.temperature_anomaly_detector.service;

import java.time.Instant;
import java.util.UUID;

public record TemperatureAnomalyDetectedEvent(
        UUID anomalyId,
        String roomId,
        String thermometerId,
        double averageTemp,
        double currentTemp,
        Instant timestamp
) {
}
