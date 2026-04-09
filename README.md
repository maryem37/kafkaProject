
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

# EventFlow Kafka Microservices Project

This repository contains a complete event-ticketing backend built with Spring Boot microservices, Kafka event streaming, MySQL persistence, and an API Gateway, plus a lightweight frontend.

It demonstrates a full booking workflow:

1. User browses events from **Inventory Service**.
2. User submits booking to **Booking Service**.
3. Booking Service validates capacity and publishes `booking_event` to Kafka.
4. **Order Service** consumes the Kafka event, creates an order, and updates inventory capacity.
5. Frontend accesses services through **API Gateway**.

---

## 1) Repository Structure

- `project/` → Inventory Service (catalog, event capacity, Flyway migrations, docker assets)
- `bookingService/bookingService/` → Booking Service (booking API, Kafka producer)
- `Order/Order/` → Order Service (Kafka consumer, order persistence)
- `apigateway/api-gateway/` → API Gateway (routing + CORS)
- `frontend/` → Static UI (`index.html`)

---

## 2) Tech Stack

- Java 17
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Spring Boot 4.0.5
- Spring Data JPA
- Spring Cloud Gateway MVC (API Gateway)
- Apache Kafka + Zookeeper
- MySQL 8
- Flyway (schema migration in inventory service)
- Lombok
>>>>>>> 81e756b914a5a5114784b867f587f74756bbea65
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

## 3) Services and Ports

| Component | Port | Purpose |
|---|---:|---|
| API Gateway | `8090` | Single entry point + route forwarding |
| Inventory Service | `8080` | Event and venue inventory APIs |
| Booking Service | `8081` | Booking creation API + Kafka producer |
| Order Service | `8082` | Kafka consumer, order persistence, inventory update |
| MySQL (Docker) | `3307` (host) → `3306` (container) | Primary database |
| Kafka Broker | `9092` | Kafka for app services (host) |
| Kafka UI | `8084` | Inspect topics/messages |
| Schema Registry | `8083` | Kafka schema registry |
| Zookeeper | `2181` | Kafka dependency |

---

## 4) High-Level Architecture

```text
Frontend (index.html)
    |
    v
API Gateway (8090)
    |-----------------------> Inventory Service (8080) ----> MySQL
    |
    |-----------------------> Booking Service (8081) -------> MySQL
                                  |
                                  | publish booking_event
                                  v
                                Kafka (9092)
                                  |
                                  | consume booking_event
                                  v
                           Order Service (8082) -----------> MySQL
                                  |
                                  | REST call
                                  v
                         Inventory Service capacity update
```

---

## 5) Database Schema (Flyway)

Flyway scripts are in `project/src/main/resources/db/migration`:

- `V1__init.sql`
  - Creates `venue`
  - Creates `event` with FK to `venue`
- `V2__add_ticket_column_in_event_table.sql`
  - Adds `ticket_price` to `event`
- `V3__create__customer_table.sql`
  - Creates `customer`
- `V4__create_order_table.sql`
  - Creates ``order`` with FK to `customer` and `event`

MySQL init script (`project/docker/mysql/init.sql`) creates database `ticketing`.

> Note: In `project/docker-compose.yml`, `MYSQL_DATABASE` is `tickiting` (typo), while app configs use `ticketing`. The JDBC URL includes `createDatabaseIfNotExist=true`, so `ticketing` can still be created at runtime.

---

## 6) API Endpoints

### Inventory Service (`/api/v1/inventory/...`)

- `GET /api/v1/inventory/events`
  - List all events with venue and remaining capacity.
- `GET /api/v1/inventory/venue/{venueId}`
  - Venue details by ID.
- `GET /api/v1/inventory/event/{eventId}`
  - Event details by ID.
- `PUT /api/v1/inventory/event/{eventId}/capacity/{capacity}`
  - Decrements remaining capacity by `capacity` (used by Order Service).

### Booking Service

- `POST /api/v1/booking`
  - Creates a booking request.
  - Validates customer existence.
  - Validates inventory capacity.
  - Publishes Kafka event `booking_event`.
  - Returns booking summary.

Request body:


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

  "eventId": 2,
  "ticketCount": 3
}
```

Response body:

```json
{
  "userId": 1,
  "eventId": 2,
  "ticketCount": 3,
  "totalPrice": 150.00
}
```

### Order Service

No direct REST controller is currently exposed. It works asynchronously as a Kafka consumer:

- listens to topic `booking_event` with group `order-service`
- deserializes message to `BookingEvent`
- persists order in table ``order``
- calls Inventory Service to reduce available seats

---

## 7) API Gateway Routing

Gateway routes (configured in `apigateway/api-gateway/src/main/resources/application.properties`):

- `/api/v1/inventory/**` → `http://localhost:8080`
- `/api/v1/booking/**` → `http://localhost:8081`
- `/api/v1/order/**` → `http://localhost:8082`

CORS is globally enabled with permissive configuration to simplify local frontend usage.

---

## 8) Prerequisites

Install the following:

- Java 17
- Docker Desktop
- Maven (optional, wrappers are included)
- (Optional) Postman or similar API client

---

## 9) Run the Project Locally (Step by Step)

## 9.1 Start infrastructure

From `project/`:

```bash
docker compose up -d
```

This starts MySQL, Zookeeper, Kafka broker, Kafka UI, Schema Registry.

## 9.2 Start backend services

Start each service in a separate terminal.

### Inventory Service

```bash
cd project
./mvnw spring-boot:run
```
(Windows: `mvnw.cmd spring-boot:run`)

### Booking Service

```bash
cd bookingService/bookingService
./mvnw spring-boot:run
```
(Windows: `mvnw.cmd spring-boot:run`)

### Order Service

```bash
cd Order/Order
./mvnw spring-boot:run
```
(Windows: `mvnw.cmd spring-boot:run`)

### API Gateway

```bash
cd apigateway/api-gateway
./mvnw spring-boot:run
```
(Windows: `mvnw.cmd spring-boot:run`)

## 9.3 Open frontend

Open `frontend/index.html` in a browser.

By default it targets:

- `http://localhost:8090/api/v1`

You can override the API base via query parameter:

- `index.html?api=http://localhost:8090/api/v1`

---

## 10) Verify End-to-End Flow

1. Call gateway inventory endpoint:
   - `GET http://localhost:8090/api/v1/inventory/events`
2. Submit booking:
   - `POST http://localhost:8090/api/v1/booking`
3. Check Kafka UI:
   - `http://localhost:8084`
4. Validate DB updates:
   - New row in ``order`` table
   - `event.left_capacity` decreased

---

## 11) Sample Data (Optional)

If your DB is empty, insert seed data manually:

```sql
USE ticketing;

INSERT INTO venue (name, address, total_capacity)
VALUES
  ('Olympia Hall', 'Downtown', 1000),
  ('Open Air Arena', 'North City', 5000);

INSERT INTO event (name, venue_id, total_capacity, left_capacity, ticket_price)
VALUES
  ('Spring Music Festival', 1, 1000, 1000, 50.00),
  ('Tech Conference 2026', 2, 5000, 5000, 120.00);

INSERT INTO customer (name, email, address)
VALUES
  ('Alice Doe', 'alice@example.com', 'Street 1'),
  ('Bob Smith', 'bob@example.com', 'Street 2');
```

---

## 12) Build and Test

Per service:

```bash
./mvnw clean test
./mvnw clean package
```

(Windows: replace with `mvnw.cmd ...`)

---

## 13) Configuration Summary

All services use MySQL datasource:

- URL: `jdbc:mysql://localhost:3307/ticketing?createDatabaseIfNotExist=true`
- Username: `root`
- Password: `password`

Kafka settings:

- Bootstrap server: `localhost:9092`
- Producer topic used by booking: `booking_event`
- Consumer group used by order: `order-service`

---

## 14) Known Issues / Notes

- There are some naming typos in code and folders (`contolleur`, `BookingContolleur`, `tickiting` in compose env). The project can still run, but cleanup is recommended.
- `Order Service` has no REST controller at the moment; it is event-driven.
- Ensure all four backend apps are running before testing through the frontend.
- Kafka broker must be available before booking/order interactions.

---

## 15) Suggested Improvements

- Add `README` per service and a parent aggregator build.
- Add Dockerfiles and a unified compose for all apps.
- Add OpenAPI/Swagger for Inventory and Booking APIs.
- Add validation/error handling (`@ControllerAdvice`).
- Add integration tests for booking → kafka → order → inventory flow.
- Add monitoring (`Actuator`, Prometheus, Grafana).

---

## 16) Quick Command Reference

```bash
# Infra
cd project
docker compose up -d

# Services (in separate terminals)
cd project && mvnw.cmd spring-boot:run
cd bookingService/bookingService && mvnw.cmd spring-boot:run
cd Order/Order && mvnw.cmd spring-boot:run
cd apigateway/api-gateway && mvnw.cmd spring-boot:run
```

---

## 17) Ownership / Contribution

This project appears to be an educational microservices + Kafka ticketing implementation. Feel free to fork and improve architecture, observability, and production-readiness.

