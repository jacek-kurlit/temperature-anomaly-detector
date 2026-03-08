"""
Fetches Kafka metrics from the actuator and prints:
 - Per-topic listener throughput and average processing time
 - Per-topic consumer lag (events waiting in queue)
"""
import re
import urllib.request

BASE_URL = "http://localhost:8080/actuator/prometheus"

LISTENER_URL = f"{BASE_URL}?includedNames=spring_kafka_listener_seconds"
LAG_URL = f"{BASE_URL}?includedNames=kafka_consumer_fetch_manager_records_lag"

LISTENER_RE = re.compile(
    r'^spring_kafka_listener_seconds_(count|sum)'
    r'\{[^}]*messaging_source_name="([^"]+)"[^}]*\}'
    r'\s+([\d.eE+\-]+)'
)

LAG_RE = re.compile(
    r'^kafka_consumer_fetch_manager_records_lag'
    r'\{[^}]*topic="([^"]+)"[^}]*\}'
    r'\s+([\d.eE+\-]+)'
)


def fetch(url: str) -> str:
    with urllib.request.urlopen(url) as resp:
        return resp.read().decode()


def fetch_listener_metrics() -> dict[str, dict[str, float]]:
    """Returns {topic: {count, sum}}"""
    topics: dict[str, dict[str, float]] = {}
    for line in fetch(LISTENER_URL).splitlines():
        m = LISTENER_RE.match(line)
        if not m:
            continue
        metric_type, topic, value = m.group(1), m.group(2), float(m.group(3))
        topics.setdefault(topic, {"count": 0.0, "sum": 0.0})
        topics[topic][metric_type] = value
    return topics


def fetch_lag_metrics() -> dict[str, float]:
    """Returns {topic: total_lag} summed across all partitions"""
    lag: dict[str, float] = {}
    for line in fetch(LAG_URL).splitlines():
        m = LAG_RE.match(line)
        if not m:
            continue
        topic, value = m.group(1), float(m.group(2))
        lag[topic] = lag.get(topic, 0.0) + value
    return lag


def main():
    listeners = fetch_listener_metrics()
    lag = fetch_lag_metrics()

    all_topics = sorted(listeners.keys() | lag.keys())

    print(f"{'Topic':<30} {'Consumed':>10} {'Total (ms)':>12} {'Avg/event (ms)':>16} {'Lag':>8}")
    print("-" * 80)
    for topic in all_topics:
        data = listeners.get(topic, {"count": 0.0, "sum": 0.0})
        count = data["count"]
        total_ms = data["sum"] * 1000
        avg_ms = total_ms / count if count else 0
        topic_lag = lag.get(topic, 0.0)
        print(f"{topic:<30} {count:>10.0f} {total_ms:>12.2f} {avg_ms:>16.3f} {topic_lag:>8.0f}")


if __name__ == "__main__":
    main()
