-- Alter events table to change source column from text to integer
-- Handle existing data by converting text values to integers
-- If source is already a numeric string, use it directly
-- Otherwise, set a default value (0) for any non-numeric values

alter table events 
alter column source type integer 
using case 
    when source ~ '^[0-9]+$' then source::integer
    else 0
end;
