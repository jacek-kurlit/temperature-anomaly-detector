"""
Stress test: 10 rooms × 10 thermometers = 100 thermometers
Target: 20,000 events/sec total → 200 events/sec per thermometer
Duration: 10 seconds → 200,000 events total
"""
import json
import random
import time
from datetime import datetime, timezone

from confluent_kafka import Producer

BOOTSTRAP_SERVERS = "localhost:9092"
TOPIC = "temperature-measurements"

ROOMS = 10
THERMOMETERS_PER_ROOM = 10
TARGET_RATE = 20_000       # events/sec total
DURATION_SECONDS = 10

THERMOMETERS = [
    (f"room-{r:02d}", f"thermo-{r:02d}-{t:02d}")
    for r in range(1, ROOMS + 1)
    for t in range(1, THERMOMETERS_PER_ROOM + 1)
]
TOTAL_THERMOMETERS = len(THERMOMETERS)   # 100


def make_event(thermometer_id: str, room_id: str) -> bytes:
    return json.dumps({
        "thermometerId": thermometer_id,
        "roomId": room_id,
        "temperature": round(random.uniform(18.0, 28.0), 2),
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }).encode()


def on_delivery(err, msg):
    if err:
        print(f"Delivery failed for {msg.key()}: {err}")


def main():
    producer = Producer({
        "bootstrap.servers": BOOTSTRAP_SERVERS,
        "linger.ms": 5,
        "batch.size": 524288,       # 512 KB
        "compression.type": "lz4",
        "acks": 1,
        "queue.buffering.max.messages": 2_000_000,
    })

    interval = 1.0 / TARGET_RATE   # ideal time between events

    print(f"Stress test starting:")
    print(f"  Rooms:           {ROOMS}")
    print(f"  Thermometers:    {TOTAL_THERMOMETERS} ({THERMOMETERS_PER_ROOM} per room)")
    print(f"  Target rate:     {TARGET_RATE:,} events/sec ({TARGET_RATE // TOTAL_THERMOMETERS} per thermometer/sec)")
    print(f"  Duration:        {DURATION_SECONDS}s")
    print(f"  Expected total:  {TARGET_RATE * DURATION_SECONDS:,} events\n")

    sent = 0
    dropped = 0
    deadline = time.monotonic() + DURATION_SECONDS
    next_send = time.monotonic()
    report_at = time.monotonic() + 1.0
    start = time.monotonic()

    while time.monotonic() < deadline:
        now = time.monotonic()

        if now < next_send:
            # spin-wait to preserve sub-millisecond timing accuracy
            continue

        room_id, thermometer_id = THERMOMETERS[sent % TOTAL_THERMOMETERS]
        try:
            producer.produce(
                topic=TOPIC,
                key=thermometer_id,
                value=make_event(thermometer_id, room_id),
                headers={"__TypeId__": "temperature-measurement"},
                on_delivery=on_delivery,
            )
            sent += 1
        except BufferError:
            # producer queue full — librdkafka can't keep up, skip this event
            dropped += 1

        producer.poll(0)
        next_send += interval

        if now >= report_at:
            elapsed = now - start
            print(f"  [{elapsed:5.1f}s] sent={sent:,}  dropped={dropped}  rate={sent / elapsed:,.0f} events/sec")
            report_at += 1.0

    producer.flush()

    elapsed = time.monotonic() - start
    print(f"\nDone.")
    print(f"  Sent:    {sent:,}")
    print(f"  Dropped: {dropped}")
    print(f"  Elapsed: {elapsed:.2f}s")
    print(f"  Rate:    {sent / elapsed:,.0f} events/sec")


if __name__ == "__main__":
    main()
