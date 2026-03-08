import argparse
import json
import random
import uuid
from datetime import datetime, timezone

from confluent_kafka import Producer

BOOTSTRAP_SERVERS = "localhost:9092"
TOPIC = "temperature-measurements"


def make_event(thermometer_id: str, room_id: str, temp_from: float, temp_to: float) -> bytes:
    return json.dumps({
        "thermometerId": thermometer_id,
        "roomId": room_id,
        "temperature": round(random.uniform(temp_from, temp_to), 2),
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }).encode()


def on_delivery(err, msg):
    if err:
        print(f"Delivery failed for key={msg.key()}: {err}")
    else:
        print(f"Delivered key={msg.key().decode()} partition={msg.partition()} offset={msg.offset()}")


def main():
    parser = argparse.ArgumentParser(description="Temperature measurement Kafka producer")
    parser.add_argument("--thermometer-id", default=f"thermo-{uuid.uuid4()}", help="Thermometer ID (default: random UUID)")
    parser.add_argument("--room-id", default=f"room-{uuid.uuid4()}", help="Room ID (default: random UUID)")
    parser.add_argument("--size", type=int, required=True, help="Number of events to send")
    parser.add_argument("--temp-from", type=float, required=True, help="Minimum temperature")
    parser.add_argument("--temp-to", type=float, required=True, help="Maximum temperature")
    args = parser.parse_args()

    if args.temp_from > args.temp_to:
        parser.error("--temp-from must be less than or equal to --temp-to")

    producer = Producer({"bootstrap.servers": BOOTSTRAP_SERVERS})

    print(f"Producing {args.size} events to '{TOPIC}'")
    print(f"  thermometerId: {args.thermometer_id}")
    print(f"  roomId:        {args.room_id}")
    print(f"  temperature:   [{args.temp_from}, {args.temp_to}]\n")

    for _ in range(args.size):
        producer.produce(
            topic=TOPIC,
            key=args.thermometer_id,
            value=make_event(args.thermometer_id, args.room_id, args.temp_from, args.temp_to),
            headers={"__TypeId__": "temperature-measurement"},
            on_delivery=on_delivery,
        )
        producer.poll(0)

    producer.flush()
    print(f"\nDone. Sent {args.size} events.")


if __name__ == "__main__":
    main()
