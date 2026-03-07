# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run tests (requires Docker for Testcontainers)
./mvnw test

# Run a single test class
./mvnw test -Dtest=TemperatureAnomalyDetectorApplicationTests

# Run the application (requires running Kafka and MongoDB)
./mvnw spring-boot:run

# Run with Testcontainers dev services (spins up Kafka + MongoDB via Docker)
./mvnw spring-boot:test-run
```

## Architecture

This is a **Spring Boot 4.0.3** application targeting **Java 25** in early development. The intended purpose is temperature anomaly detection using an event-driven architecture.

**Core stack:**
- **Kafka** (`spring-boot-starter-kafka`) — event ingestion/processing pipeline
- **MongoDB** (`spring-boot-starter-data-mongodb`) — persistence layer
- **Spring Web MVC** (`spring-boot-starter-webmvc`) — REST API
- **Spring Security** (`spring-boot-starter-security`) — authentication/authorization
- **Actuator + Prometheus** — observability via `/actuator/prometheus`
- **Micrometer Tracing + Brave** — distributed tracing

**Testing approach:**
- Integration tests use **Testcontainers** with `@ServiceConnection` for automatic wiring — no manual connection config needed
- `TestcontainersConfiguration` spins up `apache/kafka-native:latest` and `mongo:latest`
- `TestTemperatureAnomalyDetectorApplication` enables running the app locally with Docker-managed services

**Package:** `com.always.right.inc.temperature_anomaly_detector` (underscore, not hyphen — package naming constraint from Spring Initializr)

**Note:** The project is freshly scaffolded. Domain logic (anomaly detection, Kafka consumers/producers, MongoDB repositories, REST controllers) has not been implemented yet.
