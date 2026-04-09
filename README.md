# EventFlow Kafka Microservices (Ticketing Demo)

This repository is an educational microservices project that demonstrates:

1. A ticket booking workflow implemented with Spring Boot services.
2. Event-driven communication with Kafka (producers/consumers, consumer groups, fan-out).
3. MySQL persistence with Flyway migrations (single migration owner pattern).
4. A Spring Cloud Gateway API Gateway as a single entry point.
5. A simple static frontend that talks to the gateway.
6. Optional email notification on payment completion (Mailtrap SMTP sandbox).

The core flow is:

1. User lists events from Inventory.
2. User creates a customer (name/email) in Booking service.
3. User posts a booking request to Booking service (via the gateway).
4. Booking publishes `booking_event`.
5. Order consumes `booking_event`, creates an order, then publishes `order_created_event`.
6. Inventory consumes `order_created_event` and publishes `inventory_updated_event`.
7. Payment consumes `order_created_event` (fan-out) and publishes `payment_completed_event` and can send an email.
8. Booking consumes `inventory_updated_event` to keep the UI responsive.

An architecture diagram is available in `docs/kafka-architecture.md`.

---

## Repository Structure

- `project/` → Inventory Service (events/venues, capacity updates, Flyway migrations, docker compose)
- `bookingService/bookingService/` → Booking Service (booking API + customer API + Kafka producer/consumer)
- `Order/Order/` → Order Service (Kafka consumer, creates orders, Kafka producer)
- `paymentService/paymentService/` → Payment Service (Kafka consumer, persists payments, Kafka producer, optional Mailtrap email)
- `apigateway/api-gateway/` → API Gateway (routing + permissive dev CORS)
- `frontend/` → Static UI (`index.html`, `payments.html`)
- `docs/` → Documentation and diagrams
- `kafka_2.13-3.9.1/` → Kafka distribution (optional local broker)

---

## Tech Stack

- Java 17
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Spring Boot 4.x
- Spring Data JPA
- Spring Cloud Gateway (WebMVC)
- Spring for Apache Kafka
- MySQL 8 (Docker)
- Flyway migrations (owned by Inventory service)
- Tailwind CSS (frontend via CDN)

---

## Services, Ports, and Topics

### Ports

| Component | Port | Notes |
|---|---:|---|
| API Gateway | 8090 | Single entry point for the frontend/Postman |
| Inventory Service | 8080 | Events/venues + capacity updates + Flyway owner |
| Booking Service | 8081 | Customers + booking API + produces `booking_event` |
| Order Service | 8082 | Consumes `booking_event`, produces `order_created_event` |
| Payment Service | 8083 | Consumes `order_created_event`, produces `payment_completed_event` |
| Kafka Broker | 9092 | Not started by docker-compose in this repo |
| Kafka UI (Docker) | 8084 | Observability UI for topics/messages |
| MySQL (Docker) | 3307 → 3306 | DB name: `ticketing` |

### Kafka topics

- `booking_event`
- `order_created_event`
- `inventory_updated_event`
- `payment_completed_event`

Note on “2 consumers on one topic”:

`order_created_event` is consumed by both Inventory and Payment using different consumer groups. This is intentional fan-out / pub-sub.

---

## Prerequisites

- Java 17
- Docker Desktop
- A Kafka broker reachable at `localhost:9092`
  - Option A: start Kafka locally using `kafka_2.13-3.9.1/` (recommended for demos)
  - Option B: use your own Kafka (Docker, Confluent, etc.) and keep `bootstrap-servers=localhost:9092`
- Optional: Mailtrap account (only if you want to see emails)
- Optional: Postman

---

## Run Locally (Windows-friendly steps)

### 1) Start MySQL + Kafka UI (Docker)

From `project/`:

```powershell
cd project
docker compose up -d
```

This starts:

- MySQL on `localhost:3307`
- Kafka UI on `http://localhost:8084`

### 2) Start Kafka broker

If you already have a Kafka broker running on `localhost:9092`, skip this.

This repo includes Kafka in `kafka_2.13-3.9.1/`.

One-time (KRaft storage format):

1. Open PowerShell in `kafka_2.13-3.9.1/`.
2. (Recommended) Edit `config/kraft/server.properties` and set `log.dirs` to a local folder (example: `log.dirs=./kraft-combined-logs`).
3. Run:

```powershell
cd kafka_2.13-3.9.1
.\bin\windows\kafka-storage.bat random-uuid
.\bin\windows\kafka-storage.bat format -t <PASTE_UUID_HERE> -c .\config\kraft\server.properties
```

Then start the broker:

```powershell
.\bin\windows\kafka-server-start.bat .\config\kraft\server.properties
```

### 3) Start backend services

Start each service in a separate terminal:

```powershell
cd project
.\mvnw.cmd spring-boot:run
```

```powershell
cd bookingService\bookingService
.\mvnw.cmd spring-boot:run
```

```powershell
cd Order\Order
.\mvnw.cmd spring-boot:run
```

```powershell
cd paymentService\paymentService
.\mvnw.cmd spring-boot:run
```

```powershell
cd apigateway\api-gateway
.\mvnw.cmd spring-boot:run
```

### 4) Open the frontend

Open these files in a browser:

- `frontend/index.html` (events + booking)
- `frontend/payments.html` (payments view)

The frontend calls the API Gateway at `http://localhost:8090/api/v1`.

---

## Try the Demo (API examples)

All requests go through the gateway (`8090`).

### 1) List events

GET `http://localhost:8090/api/v1/inventory/events`

### 2) Create a customer

POST `http://localhost:8090/api/v1/customers`

```json
{
  "name": "Alice Doe",
  "email": "alice@example.com",
  "address": "Street 1"
}
```

### 3) Create a booking

POST `http://localhost:8090/api/v1/booking`

```json
{
  "userId": 1,
  "eventId": 1,
  "ticketCount": 2
}
```

What to expect:

- Order service creates an order in MySQL.
- Inventory updates remaining capacity.
- Payment creates a payment row and publishes `payment_completed_event`.

### 4) View payments

GET `http://localhost:8090/api/v1/payments?limit=50`

GET `http://localhost:8090/api/v1/payments/order/{orderId}`

---

## Email (Mailtrap) Setup (Optional)

Payment service can send an email when a payment is completed.

1. Create a Mailtrap inbox and copy SMTP credentials.
2. Start Payment service with environment variables:

```powershell
$env:MAILTRAP_USERNAME="<your_username>"
$env:MAILTRAP_PASSWORD="<your_password>"
cd paymentService\paymentService
.\mvnw.cmd spring-boot:run
```

Configuration is in `paymentService/paymentService/src/main/resources/application.properties`.

Notes:

- Mailtrap Sandbox captures emails in the Mailtrap UI (not a real mailbox).
- If a customer email cannot be resolved, you can set `payment.email.fallback-to`.

---

## Database / Flyway Notes

- Flyway migrations are owned by the Inventory service.
- Migration scripts live in `project/src/main/resources/db/migration` and include payment-related tables/columns.
- Payment service runs with Flyway disabled and `spring.jpa.hibernate.ddl-auto=validate`.

---

## Troubleshooting

- Booking works but Kafka listeners show “0 records”: normal when there are no new messages; create a booking to produce events.
- Kafka UI shows no cluster/topics: ensure Kafka is running on `localhost:9092` and Docker can reach it (`host.docker.internal:9092`).
- Port conflict on `8083`: this repo uses `8083` for Payment service. Stop anything else using that port.
- MySQL connection fails: ensure `docker compose up -d` is running and port `3307` is free.

---

## Build

Per service:

```powershell
.\mvnw.cmd -DskipTests package
```
