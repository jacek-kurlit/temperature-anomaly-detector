package com.always.right.inc.temperature_anomaly_detector.domain;

import java.util.Objects;

public record RoomId(String value) {

    public RoomId {
        Objects.requireNonNull(value, "RoomId cannot be null");
    }

    @Override
    public String toString() {
        return value;
    }
}
