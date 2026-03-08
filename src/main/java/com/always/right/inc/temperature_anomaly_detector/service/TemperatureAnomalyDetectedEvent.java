package com.always.right.inc.temperature_anomaly_detector.service;

import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;

import java.time.Instant;
import java.util.UUID;

public record TemperatureAnomalyDetectedEvent(
        UUID anomalyId,
        RoomId roomId,
        ThermometerId thermometerId,
        double averageTemp,
        double currentTemp,
        Instant timestamp
) {
}
