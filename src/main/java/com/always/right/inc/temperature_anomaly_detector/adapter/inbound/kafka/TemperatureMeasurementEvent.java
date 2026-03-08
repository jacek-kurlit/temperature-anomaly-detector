package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;

import java.time.Instant;

public record TemperatureMeasurementEvent(
        String thermometerId,
        String roomId,
        Double temperature,
        Instant timestamp
) {
}
