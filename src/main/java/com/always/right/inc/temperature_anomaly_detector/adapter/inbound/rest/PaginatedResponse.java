package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaginatedResponse<T> {
    List<T> content;
    long totalElements;
    int totalPages;
}
