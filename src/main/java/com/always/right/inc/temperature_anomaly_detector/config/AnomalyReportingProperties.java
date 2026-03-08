package com.always.right.inc.temperature_anomaly_detector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "anomaly-reporting")
@Component
@Data
public class AnomalyReportingProperties {
    long thermometerCountThreshold;
}

