package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.rest;

import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;

import java.time.Instant;
import java.util.UUID;

public record TemperatureAnomalyResponse(
        UUID id,
        String thermometerId,
        String roomId,
        double averageTemp,
        double currentTemp,
        Instant createdAt
) {
    static TemperatureAnomalyResponse from(TemperatureAnomaly anomaly) {
        return new TemperatureAnomalyResponse(
                anomaly.id(),
                anomaly.thermometerId().value(),
                anomaly.roomId().value(),
                anomaly.averageTemp(),
                anomaly.currentTemp(),
                anomaly.createdAt()
        );
    }
}
