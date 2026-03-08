package com.always.right.inc.temperature_anomaly_detector;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConcurrentEventProcessingE2ETest extends E2EBaseTest {

    @Test
    void shouldHandleConcurrentProducersForSameThermometerWithoutDataCorruption() throws Exception {
        // This test verifies the core architectural assumption: thermometerId is used as the Kafka
        // partition key, which guarantees all events for a given thermometer are serialized on a
        // single partition and consumed by a single thread — making the non-thread-safe detection
        // algorithm safe even when multiple producers send events concurrently.
        String thermometerId = "thermo-concurrent-001";
        String roomId = "room-concurrent-001";

        int threads = 5;
        int eventsPerThread = 20; // 100 normal events total

        // Send normal events (20°C) from multiple threads simultaneously
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch allThreadsDone = new CountDownLatch(threads);
        List<Exception> errors = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < eventsPerThread; i++) {
                        sendMeasurement(thermometerId, roomId, 20.0, Instant.now());
                    }
                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    allThreadsDone.countDown();
                }
            });
        }

        allThreadsDone.await(10, SECONDS);
        executor.shutdown();

        // Ensure all parallel events are delivered to the broker before sending the spike.
        // Because all events share the same partition key, the broker guarantees they will
        // be consumed before the spike — preserving the algorithm's sequential invariant.
        kafkaTemplate.flush();

        // A spike sent after flush will arrive at the broker after all parallel events.
        // If the algorithm state is consistent, the window contains ~20°C readings and
        // the spike (99°C) will be detected as an anomaly.
        sendMeasurement(thermometerId, roomId, 99.0, Instant.now());

        // No thread errors during concurrent production
        assertThat(errors).isEmpty();

        // Anomaly detected — algorithm state was not corrupted by concurrent producers
        await().atMost(10, SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/temperatures/{thermometerId}/anomalies", thermometerId)
                                .header("X-Api-Key", API_KEY))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                        .andExpect(jsonPath("$.content[0].thermometerId").value(thermometerId))
                        .andExpect(jsonPath("$.content[0].currentTemp").value(99.0))
        );
    }
}
