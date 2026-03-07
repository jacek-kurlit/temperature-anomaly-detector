package com.always.right.inc.temperature_anomaly_detector.domain;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document
public record TemperatureAnomaly(
        UUID id,
        String roomId,
        String thermometerId,
        Double averageTemp,
        Double currentTemp,
        Instant createdAt
) {
}
