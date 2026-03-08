package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.rest;

import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/temperatures")
@RequiredArgsConstructor
public class TemperatureController {

    private final TemperatureAnomalyQueryService queryService;

    @GetMapping("/{thermometerId}/anomalies")
    public PaginatedResponse<TemperatureAnomalyResponse> getAnomaliesByThermometer(
            @PathVariable String thermometerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return toResponse(queryService.getAnomaliesByThermometer(thermometerId, page, pageSize));
    }

    @GetMapping("/rooms/{roomId}/anomalies")
    public PaginatedResponse<TemperatureAnomalyResponse> getAnomaliesByRoom(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return toResponse(queryService.getAnomaliesByRoom(roomId, page, pageSize));
    }

    @GetMapping("/thermometers/above-threshold")
    public List<ThermometerAnomalyCountResponse> getThermometersAboveAnomalyThreshold(
            @RequestParam Instant fromDate
    ) {
        return queryService.getThermometersExceedingAnomalyThreshold(fromDate != null ? fromDate : Instant.MIN)
                .stream()
                .map(ThermometerAnomalyCountResponse::from)
                .toList();
    }

    private PaginatedResponse<TemperatureAnomalyResponse> toResponse(Page<TemperatureAnomaly> page) {
        Page<TemperatureAnomalyResponse> mapped = page.map(TemperatureAnomalyResponse::from);
        return new PaginatedResponse<>(mapped.getContent(), mapped.getTotalElements(), mapped.getTotalPages());
    }

}
