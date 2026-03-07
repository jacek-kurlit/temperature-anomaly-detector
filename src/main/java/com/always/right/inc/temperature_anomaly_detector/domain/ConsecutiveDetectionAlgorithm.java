package com.always.right.inc.temperature_anomaly_detector.domain;

public class ConsecutiveDetectionAlgorithm implements AnomalyDetectionAlgorithm {

    @Override
    public boolean accept(TemperatureMeasurement measurement) {
        return false;
    }

}
