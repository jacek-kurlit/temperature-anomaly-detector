package com.always.right.inc.temperature_anomaly_detector.domain;

import java.util.Optional;

public class TimeWindowDetectionAlgorithm implements AnomalyDetectionAlgorithm {

    @Override
    public Optional<AnomalyDetectionResult> accept(TemperatureMeasurement measurement) {
        throw new IllegalStateException("Not implemented");
    }
}
