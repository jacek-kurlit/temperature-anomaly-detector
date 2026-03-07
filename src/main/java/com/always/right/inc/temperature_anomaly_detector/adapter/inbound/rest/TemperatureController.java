package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/temperatures")
public class TemperatureController {

    @GetMapping
    public String status() {
        log.info("Status requested");
        return "ok";
    }
}
