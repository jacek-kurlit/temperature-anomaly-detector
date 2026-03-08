package com.always.right.inc.temperature_anomaly_detector.domain;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Identify a temperature measurement as an anomaly if, within any n consecutive
 * temperature measurements, there exists one measurement that surpasses the
 * average of the remaining n-1 measurements by 5.0 degrees Celsius.
 * <p>
 * Technical notes:
 * First we are collection n measurements, after that we start to evaluate
 * if there is an anomaly returning first occurrence.
 * Potential issue:
 * 1. One anomaly can be detected in worst case n times.
 * 2. We need to wait for new measurement to evaluate if there is an anomaly.
 * 3. Sudden arrival of very high temperature can cause false positive.
 */
public class ConsecutiveDetectionAlgorithm implements AnomalyDetectionAlgorithm {

    private final Deque<Double> window;
    private final int windowSize;
    private final double threshold;
    private double currentSum;

    public ConsecutiveDetectionAlgorithm(int windowSize, double threshold) {
        this.windowSize = windowSize;
        this.window = new ArrayDeque<>(windowSize);
        this.currentSum = 0;
        this.threshold = threshold;
    }

    @Override
    public Optional<AnomalyDetectionResult> accept(TemperatureMeasurement measurement) {
        window.addLast(measurement.value());
        currentSum += measurement.value();

        if (window.size() < windowSize) {
            return Optional.empty();
        }
        Optional<AnomalyDetectionResult> anomaly = findAnomaly();
        currentSum -= window.pollFirst();
        return anomaly;
    }

    private Optional<AnomalyDetectionResult> findAnomaly() {
        for (double candidate : window) {
            double avgOfOthers = (currentSum - candidate) / (windowSize - 1);
            boolean isAnomaly = Math.abs(candidate - avgOfOthers) > threshold;
            if (isAnomaly) {
                return Optional.of(new AnomalyDetectionResult(candidate, avgOfOthers));
            }
        }
        return Optional.empty();
    }
}
