create table users (
                       id uuid primary key,
                       phone_number text not null unique,
                       timezone text not null,
                       created_at timestamptz not null
);

create table events (
                        id uuid primary key,
                        user_id uuid not null references users(id),
                        title text not null,
                        description text,
                        start_time timestamptz not null,
                        end_time timestamptz,
                        source text not null,
                        created_at timestamptz not null
);

create table reminders (
                           id uuid primary key,
                           event_id uuid not null references events(id),
                           user_id uuid not null references users(id),
                           remind_at timestamptz not null,
                           status text not null,
                           channel text not null,
                           created_at timestamptz not null
);

create index idx_reminder_due
    on reminders (remind_at, status);
