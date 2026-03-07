package com.always.right.inc.temperature_anomaly_detector.domain;

import java.util.Objects;

public record ThermometerId(String value) {
    public ThermometerId {
        Objects.requireNonNull(value, "ThermometerId cannot be null");
    }

    @Override
    public String toString() {
        return value;
    }
}
