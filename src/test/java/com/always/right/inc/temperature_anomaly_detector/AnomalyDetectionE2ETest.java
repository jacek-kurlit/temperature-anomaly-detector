package com.always.right.inc.temperature_anomaly_detector;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnomalyDetectionE2ETest extends E2EBaseTest {

    @Test
    void shouldDetectSpikeAnomalyAndExposeItViaThermometerEndpoint() {
        // given
        String thermometerId = "thermo-001";
        String roomId = "room-001";

        // 10 normal readings at 20°C, then a spike at 30.5°C
        // avg of 9 = 20.0, deviation = 10.5 > threshold(5.0) → anomaly
        fillMeasurementWindowWith(thermometerId, roomId, 20.0);
        sendMeasurement(thermometerId, roomId, 30.5, Instant.now());

        // then: anomaly flows through detection → Kafka → listener → MongoDB → REST
        await().atMost(10, SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/temperatures/{thermometerId}/anomalies", thermometerId)
                                .header("X-Api-Key", API_KEY))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content[0].thermometerId").value(thermometerId))
                        .andExpect(jsonPath("$.content[0].roomId").value(roomId))
                        .andExpect(jsonPath("$.content[0].currentTemp").value(30.5))
                        .andExpect(jsonPath("$.content[0].averageTemp").value(20.0))
        );
    }

    private void fillMeasurementWindowWith(String thermometerId,
                                           String roomId,
                                           double temperature) {
        for (int i = 0; i < WINDOW_SIZE; i++) {
            sendMeasurement(thermometerId, roomId, temperature, Instant.now());
        }
    }

    @Test
    void shouldDetectAnomalyAndExposeItViaRoomEndpoint() {
        String thermometerId = "thermo-002";
        String roomId = "room-002";
        Instant base = Instant.now();

        fillMeasurementWindowWith(thermometerId, roomId, 20.0);
        sendMeasurement(thermometerId, roomId, 30.5, base.plusSeconds(9));

        await().atMost(10, SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/temperatures/rooms/{roomId}/anomalies", roomId)
                                .header("X-Api-Key", API_KEY))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content[0].thermometerId").value(thermometerId))
                        .andExpect(jsonPath("$.content[0].roomId").value(roomId))
                        .andExpect(jsonPath("$.content[0].currentTemp").value(30.5))
        );
    }

    @Test
    void shouldAppearInAboveThresholdEndpointWhenAnomalyCountExceedsConfig() {
        String thermometerId = "thermo-003";
        String roomId = "room-003";
        Instant base = Instant.now();
        Instant testStart = base.minusSeconds(1);

        fillMeasurementWindowWith(thermometerId, roomId, 20.0);
        fillMeasurementWindowWith(thermometerId, roomId, 40.5);

        await().atMost(20, SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/temperatures/thermometers/above-threshold")
                                .header("X-Api-Key", API_KEY)
                                .param("fromDate", testStart.toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[?(@.thermometerId == '" + thermometerId + "')].anomalyCount").value(9))
        );
    }

    @Test
    void shouldNotDetectAnomalyWhenAllReadingsAreWithinThreshold() {
        String thermometerId = "thermo-004";
        String roomId = "room-004";

        // readings oscillating between 20.0 and 22.0 — max deviation ~1°C, well below threshold(5.0)
        for (int i = 0; i < WINDOW_SIZE; i++) {
            sendMeasurement(thermometerId, roomId, i % 2 == 0 ? 20.0 : 22.0, Instant.now());
        }

        // wait for all events to be consumed, then assert no anomaly was persisted
        await().atMost(10, SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/temperatures/{thermometerId}/anomalies", thermometerId)
                                .header("X-Api-Key", API_KEY))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(0))
        );
    }
}
