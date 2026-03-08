package com.always.right.inc.temperature_anomaly_detector.adapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "anomaly-detection")
@Component
public class DetectionProperties {
    AlgorithmType type;
    int windowSize;
    double threshold;
}
