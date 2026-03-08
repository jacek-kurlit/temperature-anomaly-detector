package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.rest;

import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerAnomalyCount;

public record ThermometerAnomalyCountResponse(String thermometerId, long anomalyCount) {

    static ThermometerAnomalyCountResponse from(ThermometerAnomalyCount count) {
        return new ThermometerAnomalyCountResponse(count.id(), count.count());
    }
}
