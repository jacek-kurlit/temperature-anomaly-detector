import json
import random
import time
from datetime import datetime, timezone

from confluent_kafka import Producer

BOOTSTRAP_SERVERS = "localhost:9092"
TOPIC = "temperature-measurements"

ROOMS = ["room1", "room2", "room3", "room4", "room5"]
THERMOMETERS = {
    "thermo-001": "living-room",
    "thermo-002": "bedroom",
    "thermo-003": "kitchen",
    "thermo-004": "bathroom",
    "thermo-005": "office",
}


def make_event(thermometer_id: str, room_id: str) -> dict:
    return {
        "temperature": round(random.uniform(25.0, 35.0), 2),
        "roomId": room_id,
        "thermometerId": thermometer_id,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


def on_delivery(err, msg):
    if err:
        print(f"Delivery failed for key={msg.key()}: {err}")
    else:
        print(
            f"Delivered key={msg.key().decode()} "
            f"partition={msg.partition()} offset={msg.offset()}"
        )


def main():
    producer = Producer({"bootstrap.servers": BOOTSTRAP_SERVERS})

    print(f"Producing to topic '{TOPIC}' on {BOOTSTRAP_SERVERS}. Ctrl+C to stop.\n")

    thermometer_id = "thermo001"
    room_id = "room1"
    event = make_event(thermometer_id, room_id)
    producer.produce(
        topic=TOPIC,
        key=thermometer_id,
        value=json.dumps(event),
        headers={"__TypeId__": "temperature-measurement"},
        on_delivery=on_delivery,
    )
    producer.poll(0)
    producer.flush()
#     try:
#         while True:
#             for thermometer_id, room_id in THERMOMETERS.items():
#                 event = make_event(thermometer_id, room_id)
#                 producer.produce(
#                     topic=TOPIC,
#                     key=thermometer_id,
#                     value=json.dumps(event),
#                     on_delivery=on_delivery,
#                 )
#             producer.poll(0)
#             time.sleep(1)
#     except KeyboardInterrupt:
#         print("\nStopping.")
#     finally:
#         producer.flush()


if __name__ == "__main__":
    main()
