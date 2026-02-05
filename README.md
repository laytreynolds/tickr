# Tickr

Tickr is a reminder and event notification service. It manages **events**, **users**, and **reminders**, and delivers reminders via **SMS** and **email** when they are due.

---

## Overview

- **Events** have an owner, optional assigned users, title, description, start/end times, and a source (e.g. API, Google).
- **Users** are identified by phone number and timezone.
- **Reminders** are created automatically when an event is created (for the owner and assigned users) at configurable offsets (e.g. 7 days, 3 days, 1 day, 4 hours before the event). Each reminder has a **channel** (SMS, EMAIL, or PHONE).
- A **scheduler** runs on a configurable interval, finds due reminders, builds channel-specific notifications, and sends them via the appropriate provider (ClickSend for SMS, JavaMail/SMTP for email).

---

## Tech Stack

| Area | Technology |
|------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 4.x |
| API | Spring Web MVC (REST) |
| Data | Spring Data JPA, PostgreSQL |
| Migrations | Flyway |
| Scheduling | Spring `@Scheduled` |
| SMS | ClickSend (HTTP API) |
| Email | Spring Mail (JavaMail / SMTP) |
| Build | Maven |

---

## Architecture

### Package layout

```
com.tickr.tickr/
├── api/                    # REST controllers (events, users, reminders, health)
├── config/                 # RestTemplate, scheduling, etc.
├── domain/                 # Core business entities and logic
│   ├── event/              # Event, EventRepository, EventService
│   ├── notification/       # Notification interface, NotificationBuilder, NotificationService
│   ├── reminder/           # Reminder, ReminderRepository, ReminderService
│   └── user/               # User, UserRepository, UserService
├── dto/                    # Request/response and notification payloads (SmsNotification, EmailNotification, etc.)
├── http/                   # HTTP client (ClickSend SMS)
├── notification/           # Senders and dispatcher (SmsNotificationSender, EmailNotificationSender, NotificationDispatcher)
└── scheduler/              # ReminderScheduler (polls and triggers sendDueReminders)
```

- **api**: Thin controllers; delegate to services.
- **domain**: Entities, repositories, and service logic. Notification *building* (Reminder → Notification) lives under `domain.notification`; actual *sending* lives under `notification`.
- **dto**: API request DTOs (snake_case via `@JsonProperty`) and notification payload DTOs (SmsNotification, EmailNotification).
- **notification**: Channel-specific senders and a dispatcher that routes a notification to the right sender.

### Request flow (high level)

1. **Create event** → `EventController` → `EventService.createEvent()` → persist event → `ReminderService.createRemindersForEvent()` creates reminders (owner + assigned users, default channel SMS).
2. **Scheduler** (fixed delay) → `ReminderService.sendDueReminders()` → for each due reminder: `NotificationService.create(reminder)` → `NotificationDispatcher.send(notification)` → appropriate sender (SMS or email).

---

## Design Decisions

### 1. Notification as an interface, channel-specific implementations

**Notification** is an interface with a single method `getChannel()`. Implementations (`SmsNotification`, `EmailNotification`) carry channel-specific payload (e.g. `to` + `body` for SMS, `to` + `subject` + `body` for email).

- **Why:** One abstraction for “something to send”; the channel and payload type vary. New channels (e.g. push) can be added by adding a new Notification implementation and a matching sender.

### 2. NotificationSender interface with `supports()` and `send()`

**NotificationSender** has `boolean supports(Notification)` and `void send(Notification)`. Implementations (`SmsNotificationSender`, `EmailNotificationSender`) decide by channel (e.g. `notification.getChannel() == Reminder.Channel.SMS`).

- **Why:** Open/closed: new channels = new sender class, no change to existing senders or to the dispatcher. The dispatcher only needs to find a sender that supports the notification and call `send()`.

### 3. NotificationDispatcher

**NotificationDispatcher** holds a `List<NotificationSender>` and implements `send(Notification)`: it finds the first sender where `supports(notification)` is true and calls `sender.send(notification)`.

- **Why:** Single entry point for sending; routing is by “who supports this notification?” instead of hard-coded channel switches. All senders are discovered via Spring’s `List<NotificationSender>` injection.

### 4. NotificationBuilder interface and NotificationService

**NotificationBuilder** has `boolean supports(Reminder)` and `Notification build(Reminder)`. Implementations (`SmsNotificationBuilder`, `EmailNotificationBuilder`) map a reminder (and its event/user) to the right Notification type (e.g. phone number + message body for SMS).

**NotificationService** holds a `List<NotificationBuilder>`, and `create(Reminder)` returns the result of the first builder that supports the reminder.

- **Why:** Building a notification from a reminder is channel-specific (recipient and body differ per channel). Builders keep that logic out of ReminderService and make it easy to add new channels. ReminderService only calls `notificationService.create(reminder)` and then `notificationDispatcher.send(notification)`.

### 5. Reminder channel on the entity

Each **Reminder** has a `channel` (SMS, PHONE, EMAIL). When creating reminders for an event, the default is SMS.

- **Why:** Same reminder pipeline (scheduler → build notification → dispatch) works for all channels; the reminder’s channel drives which builder and which sender are used. PHONE is reserved for future voice/other delivery.

### 6. Scheduler uses fixed delay, not fixed rate

The reminder job is `@Scheduled(fixedDelayString = "${scheduler.reminder.poll-rate-ms}")`: the next run starts *after* the previous run finishes.

- **Why:** Avoids overlapping runs and ensures one “send due reminders” execution completes before the next one starts, which keeps transaction and error handling predictable.

### 7. Database schema and Flyway

Schema is managed with **Flyway**; JPA is set to `ddl-auto: validate`. Column names follow Spring’s default physical naming (snake_case) so the database matches the entities.

- **Why:** Versioned, repeatable schema changes; no automatic DDL from Hibernate in production.

### 8. Configuration via environment variables

Datasource, mail, and SMS credentials are read from environment variables (e.g. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MAIL_*`, `SMS_USERNAME`, `SMS_PASSWORD`). Defaults are used only where safe (e.g. `DB_URL` default for local Postgres).

- **Why:** No secrets in config files; same codebase can run in dev, staging, and production with different env vars.

---

## API

REST API base: `http://localhost:8080/tickr/api/v1`.

| Area | Endpoints |
|------|-----------|
| Health | `GET /ping` |
| Events | `GET /event/getevents`, `POST /event/addevent`, `DELETE /event/deleteevent/{id}` |
| Users | `GET /user/getusers`, `GET /user/getuser/{id}`, `POST /user/adduser` |
| Reminders | (see `ReminderController` and `api.md`) |

Request bodies for create endpoints use **snake_case** (e.g. `owner_id`, `start_time`); responses use **camelCase**. Full API details, request/response shapes, and examples are in **[api.md](api.md)**.

---

## Configuration

Required environment variables (or overrides in `application.yml` / profiles):

| Variable | Purpose |
|----------|---------|
| `DB_URL` | JDBC URL (default: `jdbc:postgresql://localhost:5432/tickr`) |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `SMS_USERNAME` | ClickSend username |
| `SMS_PASSWORD` | ClickSend password |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP for email |

Scheduler:

- `scheduler.reminder.poll-rate-ms` – delay between reminder job runs (e.g. `60000` for 1 minute).

---

## Running the application

### Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL (for local run: create DB `tickr`, run Flyway migrations)

### Local (Maven)

```bash
export DB_USERNAME=your_user
export DB_PASSWORD=your_password
export SMS_USERNAME=your_clicksend_username
export SMS_PASSWORD=your_clicksend_password
# Optional for email:
export MAIL_HOST=smtp.example.com
export MAIL_PORT=587
export MAIL_USERNAME=...
export MAIL_PASSWORD=...

./mvnw spring-boot:run
```

API: `http://localhost:8080/tickr/api/v1/ping`

### Tests

```bash
./mvnw test
```

---

## Database

- **PostgreSQL**; schema under `src/main/resources/db/migration/` (Flyway).
- Main tables: `users`, `events`, `event_users` (join), `reminders`. JPA entities use snake_case column names via default Spring naming.

---

## Project layout (files)

```
Tickr/
├── api.md                 # Full API documentation
├── compose.yml            # (Optional) Docker Compose for app + Postgres
├── Dockerfile             # (Optional) Multi-stage build for the app
├── pom.xml
├── README.md              # This file
├── postman/               # Postman collection for the API
└── src/
    ├── main/
    │   ├── java/.../      # Application code (see package layout above)
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/   # Flyway SQL
    └── test/
```

---

## Summary

Tickr is a Spring Boot app that creates reminders from events and sends them on a schedule via SMS and email. The **Notification** and **NotificationSender** interfaces, plus **NotificationBuilder** and **NotificationDispatcher**, keep the reminder pipeline channel-agnostic and make it straightforward to add new notification types and providers without changing core reminder or event logic.
