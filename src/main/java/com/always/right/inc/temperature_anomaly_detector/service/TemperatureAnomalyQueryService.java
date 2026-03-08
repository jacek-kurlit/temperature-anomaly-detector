package com.always.right.inc.temperature_anomaly_detector.service;

import com.always.right.inc.temperature_anomaly_detector.config.AnomalyReportingProperties;
import com.always.right.inc.temperature_anomaly_detector.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemperatureAnomalyQueryService {

    private final TemperatureAnomalyRepository repository;
    private final AnomalyReportingProperties reportingProperties;

    public Page<TemperatureAnomaly> getAnomaliesByThermometer(ThermometerId thermometerId, int page, int pageSize) {
        return repository.findByThermometerIdOrderByCreatedAtDesc(thermometerId, PageRequest.of(page, pageSize));
    }

    public Page<TemperatureAnomaly> getAnomaliesByRoom(RoomId roomId, int page, int pageSize) {
        return repository.findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(page, pageSize));
    }

    public List<ThermometerAnomalyCount> getThermometersExceedingAnomalyThreshold(Instant fromDate) {
        return repository.findThermometersWithAnomalyCountExceeding(reportingProperties.getThermometerCountThreshold(), fromDate);
    }
}
