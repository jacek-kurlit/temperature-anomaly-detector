# Temperature Anomaly Detector

A Spring Boot service that ingests temperature readings from IoT thermometers via Kafka and detects anomalies, storing results in MongoDB.

## Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 25, Spring Boot 4.0.3 |
| API | Spring Web MVC |
| Messaging | Apache Kafka |
| Persistence | MongoDB |
| Security | Spring Security |
| Observability | Micrometer + Prometheus, Brave distributed tracing |
| Infrastructure | Docker Compose |
| Testing | JUnit 5, Testcontainers |

## Prerequisites

- Java 25
- Docker
- Python 3.9+ (for the event producer)

## Infrastructure Setup

Start Kafka and MongoDB:

```bash
docker compose up -d
```

Create the Kafka topic:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic temperature-measurements \
  --partitions 5 \
  --replication-factor 1
```

## Running the Application

```bash
./mvnw spring-boot:run
```

Actuator endpoints are available at `http://localhost:8080/actuator` (health, info, prometheus).

## Event Producer

The `producer/` directory contains a Python script that simulates thermometer readings by publishing events to the `temperature-measurements` Kafka topic.

### Event Schema

```json
{
  "thermometerId": "thermo-001",
  "roomId": "living-room",
  "temperature": 27.43,
  "timestamp": "2025-01-15T10:30:00.123456+00:00"
}
```

`thermometerId` is used as the Kafka message key, ensuring all readings from the same thermometer are ordered within a single partition.

### Setup

```bash
cd producer
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
```

### Run

```bash
python producer.py
```