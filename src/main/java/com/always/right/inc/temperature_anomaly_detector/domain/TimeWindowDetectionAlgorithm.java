package com.always.right.inc.temperature_anomaly_detector.domain;

public class TimeWindowDetectionAlgorithm implements AnomalyDetectionAlgorithm {

    @Override
    public boolean accept(TemperatureMeasurement measurement) {
        return false;
    }
}
