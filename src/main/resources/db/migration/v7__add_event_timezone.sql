-- Add timezone to events with safe backfill
alter table events
    add column timezone text;

update events e
set timezone = u.timezone
from users u
where e.user_id = u.id
  and e.timezone is null;

alter table events
    alter column timezone set not null;
