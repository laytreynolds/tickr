alter table users
    add column password_hash text not null default '';
