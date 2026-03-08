import json
import random
import time
from datetime import datetime, timezone

from confluent_kafka import Producer

BOOTSTRAP_SERVERS = "localhost:9092"
TOPIC = "temperature-measurements"
TARGET_RATE = 20_000  # events per second

THERMOMETERS = {
    "thermo-001": "living-room",
    "thermo-002": "bedroom",
    "thermo-003": "kitchen",
    "thermo-004": "bathroom",
    "thermo-005": "office",
}
THERMOMETER_ITEMS = list(THERMOMETERS.items())


def make_event(thermometer_id: str, room_id: str) -> bytes:
    return json.dumps({
        "temperature": round(random.uniform(25.0, 35.0), 2),
        "roomId": room_id,
        "thermometerId": thermometer_id,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }).encode()


def on_delivery(err, msg):
    if err:
        print(f"Delivery failed: {err}")


def main():
    producer = Producer({
        "bootstrap.servers": BOOTSTRAP_SERVERS,
        "linger.ms": 5,           # batch messages for up to 5ms before sending
        "batch.size": 524288,     # 512KB batch size
        "compression.type": "lz4",
        "acks": 1,                # leader ack only — faster than 'all'
        "queue.buffering.max.messages": 1_000_000,
    })

    print(f"Load test: targeting {TARGET_RATE:,} events/sec on '{TOPIC}'. Ctrl+C to stop.\n")

    sent = 0
    start = time.monotonic()
    report_interval = 5  # seconds

    try:
        while True:
            thermometer_id, room_id = THERMOMETER_ITEMS[sent % len(THERMOMETER_ITEMS)]
            producer.produce(
                topic=TOPIC,
                key=thermometer_id,
                value=make_event(thermometer_id, room_id),
                headers={"__TypeId__": "temperature-measurement"},
                on_delivery=on_delivery,
            )
            sent += 1

            # let librdkafka drain the queue without blocking
            producer.poll(0)

            elapsed = time.monotonic() - start
            if elapsed >= report_interval:
                print(f"Sent {sent:,} events in {elapsed:.1f}s → {sent / elapsed:,.0f} events/sec")
                sent = 0
                start = time.monotonic()

    except KeyboardInterrupt:
        print("\nFlushing remaining messages...")
    finally:
        producer.flush()


if __name__ == "__main__":
    main()
