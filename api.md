# Tickr API Documentation

Base URL: `http://localhost:8080/tickr/api/v1`

## Table of Contents
- [Health](#health)
- [Events](#events)
- [Users](#users)
- [Data Models](#data-models)

---

## Health

Base Path: `/tickr/api/v1`

### Ping

**GET** `/tickr/api/v1/ping`

Check if the API is running.

**Response:**
```
200 OK
"pong"
```

**Example:**
```bash
curl http://localhost:8080/tickr/api/v1/ping
```

---

## Events

Base Path: `/tickr/api/v1/event`

### Get All Events

**GET** `/tickr/api/v1/event/getevents`

Retrieve all events.

**Response:**
```json
200 OK
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "owner": {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "phoneNumber": "+1234567890",
      "timezone": "America/New_York",
      "createdAt": "2024-01-01T00:00:00Z"
    },
    "assignedUsers": [
      {
        "id": "323e4567-e89b-12d3-a456-426614174002",
        "phoneNumber": "+0987654321",
        "timezone": "America/Los_Angeles",
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "title": "Team Meeting",
    "description": "Weekly sync",
    "startTime": "2024-01-15T10:00:00Z",
    "endTime": "2024-01-15T11:00:00Z",
    "source": 1,
    "createdAt": "2024-01-10T08:00:00Z"
  }
]
```

**Example:**
```bash
curl http://localhost:8080/tickr/api/v1/event/getevents
```

**HTTPie:**
```bash
http GET http://localhost:8080/tickr/api/v1/event/getevents
```

---

### Create Event

**POST** `/tickr/api/v1/event/addevent`

Create a new event with assigned users.

**Request Body:**
```json
{
  "owner_id": "123e4567-e89b-12d3-a456-426614174000",
  "assigned_user_ids": [
    "223e4567-e89b-12d3-a456-426614174001",
    "323e4567-e89b-12d3-a456-426614174002"
  ],
  "title": "Team Meeting",
  "description": "Weekly sync",
  "start_time": "2024-01-15T10:00:00Z",
  "end_time": "2024-01-15T11:00:00Z",
  "source": "1"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `owner_id` | UUID | Yes | UUID of the event owner/creator |
| `assigned_user_ids` | Array[UUID] | No | List of user UUIDs to assign to the event |
| `title` | String | Yes | Event title |
| `description` | String | No | Event description |
| `start_time` | ISO 8601 DateTime | Yes | Event start time (UTC) |
| `end_time` | ISO 8601 DateTime | No | Event end time (UTC) |
| `source` | String | Yes | Source identifier (converted to integer) |

**Response:**
```json
200 OK
{
  "id": "456e7890-e89b-12d3-a456-426614174003",
  "owner": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "phoneNumber": "+1234567890",
    "timezone": "America/New_York",
    "createdAt": "2024-01-01T00:00:00Z"
  },
  "assignedUsers": [
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "phoneNumber": "+0987654321",
      "timezone": "America/Los_Angeles",
      "createdAt": "2024-01-01T00:00:00Z"
    },
    {
      "id": "323e4567-e89b-12d3-a456-426614174002",
      "phoneNumber": "+1122334455",
      "timezone": "Europe/London",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "title": "Team Meeting",
  "description": "Weekly sync",
  "startTime": "2024-01-15T10:00:00Z",
  "endTime": "2024-01-15T11:00:00Z",
  "source": 1,
  "createdAt": "2024-01-10T08:00:00Z"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/tickr/api/v1/event/addevent \
  -H "Content-Type: application/json" \
  -d '{
    "owner_id": "123e4567-e89b-12d3-a456-426614174000",
    "assigned_user_ids": [
      "223e4567-e89b-12d3-a456-426614174001",
      "323e4567-e89b-12d3-a456-426614174002"
    ],
    "title": "Team Meeting",
    "description": "Weekly sync",
    "start_time": "2024-01-15T10:00:00Z",
    "end_time": "2024-01-15T11:00:00Z",
    "source": "1"
  }'
```

**HTTPie:**
```bash
http POST http://localhost:8080/tickr/api/v1/event/addevent \
  owner_id=123e4567-e89b-12d3-a456-426614174000 \
  assigned_user_ids:='["223e4567-e89b-12d3-a456-426614174001","323e4567-e89b-12d3-a456-426614174002"]' \
  title="Team Meeting" \
  description="Weekly sync" \
  start_time=2024-01-15T10:00:00Z \
  end_time=2024-01-15T11:00:00Z \
  source=1
```

**Error Responses:**
- `400 Bad Request` - Invalid request data or missing required fields
- `404 Not Found` - Owner or assigned user not found

---

### Delete Event

**DELETE** `/tickr/api/v1/event/deleteevent/{id}`

Delete an event by ID.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Event ID to delete |

**Response:**
```
204 No Content
```

**Example:**
```bash
curl -X DELETE http://localhost:8080/tickr/api/v1/event/deleteevent/123e4567-e89b-12d3-a456-426614174000
```

**HTTPie:**
```bash
http DELETE http://localhost:8080/tickr/api/v1/event/deleteevent/123e4567-e89b-12d3-a456-426614174000
```

**Error Responses:**
- `404 Not Found` - Event not found

---

## Users

Base Path: `/tickr/api/v1/user`

### Get All Users

**GET** `/tickr/api/v1/user/getusers`

Retrieve all users.

**Response:**
```json
200 OK
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "phoneNumber": "+1234567890",
    "timezone": "America/New_York",
    "createdAt": "2024-01-01T00:00:00Z"
  },
  {
    "id": "223e4567-e89b-12d3-a456-426614174001",
    "phoneNumber": "+0987654321",
    "timezone": "America/Los_Angeles",
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

**Example:**
```bash
curl http://localhost:8080/tickr/api/v1/user/getusers
```

**HTTPie:**
```bash
http GET http://localhost:8080/tickr/api/v1/user/getusers
```

---

### Get User by ID

**GET** `/tickr/api/v1/user/getuser/{id}`

Retrieve a specific user by ID.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | User ID |

**Response:**
```json
200 OK
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "phoneNumber": "+1234567890",
  "timezone": "America/New_York",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Example:**
```bash
curl http://localhost:8080/tickr/api/v1/user/getuser/123e4567-e89b-12d3-a456-426614174000
```

**HTTPie:**
```bash
http GET http://localhost:8080/tickr/api/v1/user/getuser/123e4567-e89b-12d3-a456-426614174000
```

**Error Responses:**
- `404 Not Found` - User not found

---

### Create User

**POST** `/tickr/api/v1/user/adduser`

Create a new user.

**Request Body:**
```json
{
  "phone_number": "+1234567890",
  "timezone": "America/New_York"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `phone_number` | String | Yes | Unique phone number (E.164 format recommended) |
| `timezone` | String | Yes | IANA timezone identifier (e.g., "America/New_York") |

**Response:**
```json
200 OK
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "phoneNumber": "+1234567890",
  "timezone": "America/New_York",
  "createdAt": "2024-01-10T08:00:00Z"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/tickr/api/v1/user/adduser \
  -H "Content-Type: application/json" \
  -d '{
    "phone_number": "+1234567890",
    "timezone": "America/New_York"
  }'
```

**HTTPie:**
```bash
http POST http://localhost:8080/tickr/api/v1/user/adduser \
  phone_number=+1234567890 \
  timezone="America/New_York"
```

**Error Responses:**
- `400 Bad Request` - Invalid request data or missing required fields
- `409 Conflict` - Phone number already exists (if unique constraint violation)

---

## Data Models

Responses use **camelCase** (Jackson default). Request bodies for create endpoints use **snake_case** per `@JsonProperty` on the DTOs.

### Event

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Event ID |
| `owner` | User | Event owner/creator |
| `assignedUsers` | User[] | Users assigned to the event |
| `title` | string | Event title |
| `description` | string | Event description (optional) |
| `startTime` | ISO 8601 DateTime | Event start time (UTC) |
| `endTime` | ISO 8601 DateTime | Event end time (optional) |
| `source` | integer | Source identifier (e.g. API, Google, Outlook) |
| `createdAt` | ISO 8601 DateTime | Creation time (UTC) |

### User

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | User ID |
| `phoneNumber` | string | Unique phone number (E.164 recommended) |
| `timezone` | string | IANA timezone (e.g. America/New_York) |
| `createdAt` | ISO 8601 DateTime | Creation time (UTC) |

---

## Common HTTP Status Codes

| Code | Description |
|------|-------------|
| `200 OK` | Request successful |
| `204 No Content` | Request successful, no content to return |
| `400 Bad Request` | Invalid request data |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Resource conflict (e.g., duplicate phone number) |
| `500 Internal Server Error` | Server error |

---

## Notes

- All timestamps are in ISO 8601 format (UTC)
- UUIDs follow the standard UUID v4 format
- Phone numbers should be in E.164 format (e.g., +1234567890)
- Timezones should use IANA timezone identifiers (e.g., "America/New_York", "Europe/London")
- The `source` field in events is stored as an integer
- When creating events, assigned users are automatically linked via the many-to-many relationship. Reminders are created automatically for the event (owner + assigned users) with default channel SMS.
