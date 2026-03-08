package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.rest;

import com.always.right.inc.temperature_anomaly_detector.RestBaseTest;
import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerAnomalyCount;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemperatureController.class)
@WithMockUser
class TemperatureControllerTest extends RestBaseTest {

    @MockitoBean
    TemperatureAnomalyQueryService queryService;

    @Test
    void shouldReturnAnomaliesForThermometer() throws Exception {
        var thermometerId = new ThermometerId("thermo-001");
        var anomaly = new TemperatureAnomaly(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                new RoomId("room1"),
                thermometerId,
                20.0,
                27.5,
                Instant.parse("2025-01-15T10:30:00Z")
        );
        when(queryService.getAnomaliesByThermometer(thermometerId, 0, 20))
                .thenReturn(new PageImpl<>(List.of(anomaly), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/temperatures/{thermometerId}/anomalies", thermometerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].thermometerId").value(thermometerId.value()))
                .andExpect(jsonPath("$.content[0].roomId").value("room1"))
                .andExpect(jsonPath("$.content[0].averageTemp").value(20.0))
                .andExpect(jsonPath("$.content[0].currentTemp").value(27.5))
                .andExpect(jsonPath("$.content[0].createdAt").value("2025-01-15T10:30:00Z"));
    }

    @Test
    void shouldRespectPageAndPageSizeParams() throws Exception {
        var thermometerId = new ThermometerId("thermo-001");
        when(queryService.getAnomaliesByThermometer(thermometerId, 2, 5))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

        mockMvc.perform(get("/api/temperatures/{thermometerId}/anomalies", thermometerId)
                        .param("page", "2")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldUseDefaultPaginationWhenParamsAbsent() throws Exception {
        ThermometerId thermometerId = new ThermometerId("thermo-001");
        when(queryService.getAnomaliesByThermometer(thermometerId, 0, 20))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/temperatures/{thermometerId}/anomalies", thermometerId))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/temperatures/thermo-001/anomalies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAnomaliesForRoom() throws Exception {
        RoomId roomId = new RoomId("room1");
        var anomaly = new TemperatureAnomaly(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                roomId,
                new ThermometerId("thermo-001"),
                20.0,
                27.5,
                Instant.parse("2025-01-15T10:30:00Z")
        );
        when(queryService.getAnomaliesByRoom(roomId, 0, 20))
                .thenReturn(new PageImpl<>(List.of(anomaly), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/temperatures/rooms/{roomId}/anomalies", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].roomId").value(roomId.value()))
                .andExpect(jsonPath("$.content[0].thermometerId").value("thermo-001"))
                .andExpect(jsonPath("$.content[0].averageTemp").value(20.0))
                .andExpect(jsonPath("$.content[0].currentTemp").value(27.5))
                .andExpect(jsonPath("$.content[0].createdAt").value("2025-01-15T10:30:00Z"));
    }

    @Test
    void shouldRespectPageAndPageSizeParamsForRoom() throws Exception {
        var roomId = new RoomId("room1");
        when(queryService.getAnomaliesByRoom(roomId, 1, 10))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 0));

        mockMvc.perform(get("/api/temperatures/rooms/{roomId}/anomalies", roomId)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401ForRoomEndpointWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/temperatures/rooms/room1/anomalies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnThermometersAboveAnomalyThreshold() throws Exception {
        var fromDate = Instant.parse("2025-01-01T00:00:00Z");
        when(queryService.getThermometersExceedingAnomalyThreshold(fromDate))
                .thenReturn(List.of(
                        new ThermometerAnomalyCount(new ThermometerId("thermo-001"), 8),
                        new ThermometerAnomalyCount(new ThermometerId("thermo-003"), 12)
                ));

        mockMvc.perform(get("/api/temperatures/thermometers/above-threshold")
                        .param("fromDate", "2025-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].thermometerId").value("thermo-001"))
                .andExpect(jsonPath("$[0].anomalyCount").value(8))
                .andExpect(jsonPath("$[1].thermometerId").value("thermo-003"))
                .andExpect(jsonPath("$[1].anomalyCount").value(12));
    }

    @Test
    void shouldReturnEmptyListWhenNoThermometersExceedThreshold() throws Exception {
        var fromDate = Instant.parse("2025-01-01T00:00:00Z");
        when(queryService.getThermometersExceedingAnomalyThreshold(fromDate))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/temperatures/thermometers/above-threshold")
                        .param("fromDate", "2025-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturn400WhenFromDateIsMissing() throws Exception {
        mockMvc.perform(get("/api/temperatures/thermometers/above-threshold"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401ForThermometerThresholdEndpointWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/temperatures/thermometers/above-threshold")
                        .param("fromDate", "2025-01-01T00:00:00Z"))
                .andExpect(status().isUnauthorized());
    }
}
