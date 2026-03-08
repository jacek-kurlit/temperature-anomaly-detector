package com.always.right.inc.temperature_anomaly_detector.domain;

import java.util.Optional;

public interface AnomalyDetectionAlgorithm {

    Optional<AnomalyDetectionResult> accept(TemperatureMeasurement measurement);
}
