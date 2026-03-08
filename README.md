# Temperature Anomaly Detector

A Spring Boot service that ingests temperature readings from IoT thermometers via Kafka and detects anomalies, storing
results in MongoDB.

## Technology Stack

| Layer          | Technology                                         |
|----------------|----------------------------------------------------|
| Runtime        | Java 25, Spring Boot 4.0.3                         |
| API            | Spring Web MVC                                     |
| Messaging      | Apache Kafka                                       |
| Persistence    | MongoDB                                            |
| Security       | Spring Security                                    |
| Observability  | Micrometer + Prometheus, Brave distributed tracing |
| Infrastructure | Docker Compose                                     |
| Testing        | JUnit 5, Testcontainers                            |

## Assumptions and Constraints

### Thermometer IDs are globally unique

A thermometer ID must be unique across the entire system, not just within a room. The detection algorithm maintains a
separate state window per thermometer ID. If two thermometers in different rooms share the same ID, their readings will
be mixed into a single window, producing incorrect anomaly detection.

### Thermometer ID is the Kafka partition key

Each event is keyed by `thermometerId`, so all readings from the same thermometer always land on the same Kafka
partition and are consumed in order. This ordering guarantee allows detection algorithms to be implemented without locks
or thread-safety concerns, which simplifies the code and avoids synchronization overhead.

A consequence of this is that **horizontal scaling is bounded by the number of partitions** on the
`temperature-measurements` topic. If you need to scale beyond one consumer instance, increase the partition count first.

### Detection state is in-memory and non-persistent

Each algorithm instance (one per thermometer) holds its sliding window in memory. If the application restarts, all
windows reset and detection will not fire until a full window of new readings has arrived for each thermometer. There is
no warm-up replay from Kafka on startup.

### At-least-once delivery on the anomaly topic

The `temperature-anomalies` topic is consumed with at-least-once semantics. Duplicate events (e.g. after a rebalance)
are deduplicated by `anomalyId` before persisting — an anomaly already present in MongoDB is silently skipped.

### Event timestamps are producer-assigned

Timestamps in events reflect the time the producer created the event, not the time the service processed it. No
server-side validation or correction is applied.

### Anomaly persistence may be postponed
In order to speed up anomaly detection, anomaly persistence was moved to different event handler.
This has one implication, system may take some time to sync up.
In other words we traded consistency for performance.

## Prerequisites

- Java 25
- Docker
- Python 3.9+ (for the event producer)

## Infrastructure Setup

Start Kafka and MongoDB:

```bash
docker compose up -d
```

## Running the Application

I was using intellij to start 2 instances.
- One run without profile(8080)
- One with 'second' profile active(8081)

Actuator endpoints are available at `http://localhost:8080/actuator` (health, info, prometheus).

## Event Producer

The `producer/` directory contains a Python script that simulates thermometer readings by publishing events to the
`temperature-measurements` Kafka topic.

### Event Schema

```json
{
  "thermometerId": "thermo-001",
  "roomId": "living-room",
  "temperature": 27.43,
  "timestamp": "2025-01-15T10:30:00.123456+00:00"
}
```

`thermometerId` is used as the Kafka message key, ensuring all readings from the same thermometer are ordered within a
single partition.

### Setup

```bash
cd producer
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
```

### Run

Send events with explicit thermometer/room IDs and a temperature range:

```bash
python producer.py --size 50 --temp-from 18.0 --temp-to 28.0
```

All arguments:

| Argument           | Required | Default     | Description              |
|--------------------|----------|-------------|--------------------------|
| `--size`           | yes      | —           | Number of events to send |
| `--temp-from`      | yes      | —           | Minimum temperature      |
| `--temp-to`        | yes      | —           | Maximum temperature      |
| `--thermometer-id` | no       | random UUID | Thermometer ID           |
| `--room-id`        | no       | random UUID | Room ID                  |

Examples:

```bash
# send 20 events from a specific thermometer
python producer.py --thermometer-id thermo-001 --room-id living-room --size 20 --temp-from 18.0 --temp-to 28.0

# trigger an anomaly: fill the 10-event window with normal readings then send a spike
python producer.py --thermometer-id thermo-001 --room-id living-room --size 10 --temp-from 20.0 --temp-to 20.0
python producer.py --thermometer-id thermo-001 --room-id living-room --size 1  --temp-from 99.0 --temp-to 99.0
```

## Stress Test

`producer/stress_test.py` saturates the pipeline to measure throughput under load.

**Configuration:**

- 10 rooms × 10 thermometers = 100 thermometers total
- Target: 20,000 events/sec (200 events/sec per thermometer)
- Duration: 10 seconds → 200,000 events total

```bash
python stress_test.py
```

Progress is printed every second:

```
[  1.0s] sent=19,847  dropped=0  rate=19,847 events/sec
[  2.0s] sent=39,701  dropped=0  rate=19,850 events/sec
...
Done.
  Sent:    198,432
  Dropped: 0
  Elapsed: 10.01s
  Rate:    19,823 events/sec
```

`dropped` counts events skipped when the producer's internal buffer was full — a non-zero value means the consumer
pipeline cannot keep up with the send rate.

## Metrics

`producer/metrics.py` fetches Kafka listener metrics from the actuator and prints per-topic throughput and average
processing time per event.

```bash
python metrics.py
```

Example output:

```
Instance http://localhost:8080  [UP]
  Topic                            Consumed   Total (ms)   Avg/event (ms)      Lag
  ------------------------------------------------------------------------------
  temperature-anomalies               55015    122676.50            2.230     5132
  temperature-measurements           100000      5143.94            0.051        0

Instance http://localhost:8081  [UP]
  Topic                            Consumed   Total (ms)   Avg/event (ms)      Lag
  ------------------------------------------------------------------------------
  temperature-anomalies               55090    122795.00            2.229     2764
  temperature-measurements           100000      5249.95            0.052        0

Combined (all instances)
  Topic                            Consumed   Total (ms)   Avg/event (ms)      Lag
  ------------------------------------------------------------------------------
  temperature-anomalies              110105    245471.50            2.229     7896
  temperature-measurements           200000     10393.89            0.052        0
```

Useful workflow during a stress test — snapshot before, run the stress test, snapshot after, and compare the deltas:

```bash
python metrics.py          # before
python stress_test.py
python metrics.py          # after
```