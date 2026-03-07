package com.always.right.inc.temperature_anomaly_detector.domain;

public interface AnomalyDetectionAlgorithm {

    boolean accept(TemperatureMeasurement measurement);
}
