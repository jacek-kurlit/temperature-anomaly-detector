package com.always.right.inc.temperature_anomaly_detector.service;

import com.always.right.inc.temperature_anomaly_detector.domain.RoomId;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
@Slf4j
public class TemperatureAnomalyDetectionService {
    public void handle(RoomId roomId,
                       ThermometerId thermometerId,
                       Double temperature,
                       Instant timestamp) {

    }
}
