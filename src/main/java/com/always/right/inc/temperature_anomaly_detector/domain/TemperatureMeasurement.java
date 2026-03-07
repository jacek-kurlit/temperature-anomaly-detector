package com.always.right.inc.temperature_anomaly_detector.domain;

import java.time.Instant;

public record TemperatureMeasurement(double value, Instant timestamp) {
}
