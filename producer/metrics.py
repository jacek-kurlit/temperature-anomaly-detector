"""
Fetches Kafka metrics from all app instances and prints:
 - Per-instance and combined listener throughput and average processing time
 - Per-topic consumer lag (events waiting in queue), summed across instances
"""
import re
import urllib.request
from urllib.error import URLError

INSTANCES = [
    "http://localhost:8080",
    "http://localhost:8081",
]

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


def fetch(url: str) -> str | None:
    try:
        with urllib.request.urlopen(url, timeout=3) as resp:
            return resp.read().decode()
    except URLError:
        return None


def fetch_listener_metrics(base_url: str) -> dict[str, dict[str, float]]:
    body = fetch(f"{base_url}/actuator/prometheus?includedNames=spring_kafka_listener_seconds")
    topics: dict[str, dict[str, float]] = {}
    if not body:
        return topics
    for line in body.splitlines():
        m = LISTENER_RE.match(line)
        if not m:
            continue
        metric_type, topic, value = m.group(1), m.group(2), float(m.group(3))
        topics.setdefault(topic, {"count": 0.0, "sum": 0.0})
        topics[topic][metric_type] = value
    return topics


def fetch_lag_metrics(base_url: str) -> dict[str, float]:
    body = fetch(f"{base_url}/actuator/prometheus?includedNames=kafka_consumer_fetch_manager_records_lag")
    lag: dict[str, float] = {}
    if not body:
        return lag
    for line in body.splitlines():
        m = LAG_RE.match(line)
        if not m:
            continue
        topic, value = m.group(1), float(m.group(2))
        lag[topic] = lag.get(topic, 0.0) + value
    return lag


def print_table(header: str, rows: list[tuple]):
    print(f"\n{header}")
    print(f"  {'Topic':<30} {'Consumed':>10} {'Total (ms)':>12} {'Avg/event (ms)':>16} {'Lag':>8}")
    print("  " + "-" * 78)
    for topic, count, total_ms, avg_ms, lag in rows:
        print(f"  {topic:<30} {count:>10.0f} {total_ms:>12.2f} {avg_ms:>16.3f} {lag:>8.0f}")


def main():
    per_instance: list[tuple[str, dict, dict]] = []

    for instance in INSTANCES:
        listeners = fetch_listener_metrics(instance)
        lag = fetch_lag_metrics(instance)
        status = "UP" if listeners or lag else "DOWN"
        per_instance.append((instance, listeners, lag, status))

    # Per-instance tables
    for instance, listeners, lag, status in per_instance:
        all_topics = sorted(listeners.keys() | lag.keys())
        rows = []
        for topic in all_topics:
            data = listeners.get(topic, {"count": 0.0, "sum": 0.0})
            count = data["count"]
            total_ms = data["sum"] * 1000
            avg_ms = total_ms / count if count else 0.0
            rows.append((topic, count, total_ms, avg_ms, lag.get(topic, 0.0)))
        print_table(f"Instance {instance}  [{status}]", rows)

    # Combined totals
    combined_listeners: dict[str, dict[str, float]] = {}
    combined_lag: dict[str, float] = {}
    for _, listeners, lag, _ in per_instance:
        for topic, data in listeners.items():
            combined_listeners.setdefault(topic, {"count": 0.0, "sum": 0.0})
            combined_listeners[topic]["count"] += data["count"]
            combined_listeners[topic]["sum"] += data["sum"]
        for topic, value in lag.items():
            combined_lag[topic] = combined_lag.get(topic, 0.0) + value

    all_topics = sorted(combined_listeners.keys() | combined_lag.keys())
    rows = []
    for topic in all_topics:
        data = combined_listeners.get(topic, {"count": 0.0, "sum": 0.0})
        count = data["count"]
        total_ms = data["sum"] * 1000
        avg_ms = total_ms / count if count else 0.0
        rows.append((topic, count, total_ms, avg_ms, combined_lag.get(topic, 0.0)))
    print_table("Combined (all instances)", rows)


if __name__ == "__main__":
    main()
