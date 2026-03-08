package com.always.right.inc.temperature_anomaly_detector.domain;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.DoubleStream;

class ConsecutiveDetectionAlgorithmTest implements WithAssertions {

    @Test
    void shouldDetectArrivingAnomaly() {
        // given
        double threshold = 5.0;
        int windowSize = 10;
        ConsecutiveDetectionAlgorithm algorithm = init(20.0, windowSize - 1, threshold);
        // when
        Optional<AnomalyDetectionResult> anomaly = algorithm.accept(measurement(27.1));
        // then
        assertThat(anomaly).map(AnomalyDetectionResult::averageTemperature).contains(20.0);
        assertThat(anomaly).map(AnomalyDetectionResult::anomalyTemperature).contains(27.1);
    }

    @Test
    void shouldDetectAnomalyAtTheBeginningOfWindow() {
        // given
        double threshold = 5.0;
        int windowSize = 10;
        ConsecutiveDetectionAlgorithm algorithm = new ConsecutiveDetectionAlgorithm(windowSize, threshold);
        algorithm.accept(measurement(30.0));
//        [30.0,20.0 x 8] -> size(9)
        acceptValueNTimes(20.0, windowSize - 2, algorithm);

        // when
        Optional<AnomalyDetectionResult> anomaly = algorithm.accept(measurement(20.0));
        // then
        assertThat(anomaly).map(AnomalyDetectionResult::averageTemperature).contains(20.0);
        assertThat(anomaly).map(AnomalyDetectionResult::anomalyTemperature).contains(30.0);
    }

    @Test
    void shouldNotDetectAnomalyWhenAverageIsNotAboveThreshold() {
        // given
        double threshold = 5.0;
        int windowSize = 10;
        // 5X 20.0
        ConsecutiveDetectionAlgorithm algorithm = init(20.0, windowSize / 2, threshold);
        // 4X 27.0
        acceptValueNTimes(27.0, windowSize / 2 - 1, algorithm);
        // when
        Optional<AnomalyDetectionResult> anomaly = algorithm.accept(measurement(27.0));
        // then
        assertThat(anomaly).isEmpty();
    }

    @Test
    void shouldDetectAnomalyInMiddleOfWindow() {
        // given: anomaly is neither the first nor the last in the window
        // [20, 20, 20, 20, 30, 20, 20, 20, 20, 20]
        double threshold = 5.0;
        int windowSize = 10;
        ConsecutiveDetectionAlgorithm algorithm = init(20.0, windowSize / 2, threshold);
        algorithm.accept(measurement(30.0));
        acceptValueNTimes(20.0, windowSize / 2 - 1, algorithm);

        // when
        Optional<AnomalyDetectionResult> anomaly = algorithm.accept(measurement(20.0));

        // then
        assertThat(anomaly).map(AnomalyDetectionResult::anomalyTemperature).contains(30.0);
        assertThat(anomaly).map(AnomalyDetectionResult::averageTemperature).contains(20.0);
    }

    @Test
    void shouldDetectDownwardSpike() {
        // given: sudden drop is also an anomaly
        double threshold = 5.0;
        int windowSize = 10;
        ConsecutiveDetectionAlgorithm algorithm = init(20.0, windowSize - 1, threshold);

        // when
        Optional<AnomalyDetectionResult> anomaly = algorithm.accept(measurement(8.0));

        // then
        assertThat(anomaly).map(AnomalyDetectionResult::anomalyTemperature).contains(8.0);
    }

    @Test
    void shouldNotDetectAnomalyWhenDifferenceIsExactlyAtThreshold() {
        // given: threshold is exclusive — value must strictly exceed it
        double threshold = 5.0;
        int windowSize = 10;
        ConsecutiveDetectionAlgorithm algorithm = init(20.0, windowSize - 1, threshold);

        // when: 20.0 + 5.0 = 25.0 — exactly at threshold, not above
        Optional<AnomalyDetectionResult> anomaly = algorithm.accept(measurement(25.0));

        // then
        assertThat(anomaly).isEmpty();
    }

    @Test
    void shouldReturnEmptyUntilWindowIsFull() {
        // given
        int windowSize = 10;
        ConsecutiveDetectionAlgorithm algorithm = new ConsecutiveDetectionAlgorithm(windowSize, 5.0);

        // when/then: first windowSize-1 measurements never produce a result
        DoubleStream.generate(() -> 999.0) // extreme value — would be anomaly if window were full
                .limit(windowSize - 1)
                .boxed()
                .map(ConsecutiveDetectionAlgorithmTest::measurement)
                .forEach(m -> assertThat(algorithm.accept(m)).isEmpty());
    }

    @Test
    void shouldReDetectSameAnomalyUntilItIsEvictedFromWindow() {
        // given: anomaly at start is detected repeatedly as the window slides
        // until the anomaly is evicted after windowSize-1 subsequent measurements
        double threshold = 5.0;
        int windowSize = 10;
        ConsecutiveDetectionAlgorithm algorithm = new ConsecutiveDetectionAlgorithm(windowSize, threshold);
        algorithm.accept(measurement(30.0));
        acceptValueNTimes(20.0, windowSize - 1, algorithm); // fills window: [30, 20x9]

        // 30 should be detected on each of the next windowSize-1 slides
        for (int i = 0; i < windowSize - 1; i++) {
            Optional<AnomalyDetectionResult> anomaly = algorithm.accept(measurement(20.0));
            assertThat(anomaly).as("slide %d", i)
                    .map(AnomalyDetectionResult::anomalyTemperature)
                    .contains(30.0);
        }

        // once 30 is fully evicted the window is [20x10] — no more anomalies
        Optional<AnomalyDetectionResult> afterEviction = algorithm.accept(measurement(20.0));
        assertThat(afterEviction).isEmpty();
    }


    // --- helpers ---

    private static TemperatureMeasurement measurement(Double value) {
        return new TemperatureMeasurement(value, null);
    }

    private ConsecutiveDetectionAlgorithm init(double initValue, int windowSize, double threshold) {
        ConsecutiveDetectionAlgorithm algorithm = new ConsecutiveDetectionAlgorithm(windowSize, threshold);
        acceptValueNTimes(initValue, windowSize, algorithm);
        return algorithm;
    }

    private static void acceptValueNTimes(double value, int windowSize, ConsecutiveDetectionAlgorithm algorithm) {
        DoubleStream
                .generate(() -> value)
                .limit(windowSize)
                .boxed()
                .map(ConsecutiveDetectionAlgorithmTest::measurement)
                .forEach(algorithm::accept);
    }
}
