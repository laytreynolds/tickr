-- Create join table for many-to-many relationship between events and users
-- This allows multiple users to be assigned to a single event
create table event_users (
    event_id uuid not null references events(id) on delete cascade,
    user_id uuid not null references users(id) on delete cascade,
    primary key (event_id, user_id)
);

-- Create index for efficient lookups
create index idx_event_users_event_id on event_users(event_id);
create index idx_event_users_user_id on event_users(user_id);
