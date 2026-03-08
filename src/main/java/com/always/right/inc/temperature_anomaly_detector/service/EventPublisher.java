package com.always.right.inc.temperature_anomaly_detector.service;

public interface EventPublisher {

    void publish(TemperatureAnomalyDetectedEvent event);
}
