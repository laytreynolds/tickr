-- Align schema with JPA entities (Spring default physical naming: snake_case).
-- Ensures Hibernate validate matches the database.

-- Revert users column to phone_number for SpringPhysicalNamingStrategy.
-- (v4 renamed to phoneNumber; PostgreSQL stores unquoted identifiers as lowercase, so column is phonenumber.)
alter table users
    rename column phonenumber to phone_number;

-- Add index expected by User entity (@Index columnList = "phoneNumber" -> phone_number)
create index if not exists idx_user_phone_number on users(phone_number);
