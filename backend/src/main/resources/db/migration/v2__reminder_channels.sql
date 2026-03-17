-- Multi-channel reminders:
-- - reminder_channels: desired channels for a reminder
-- - reminder_deliveries: per-channel delivery log for attempts/outcomes

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public.reminder_channels (
    id uuid NOT NULL,
    reminder_id uuid NOT NULL,
    channel character varying(32) NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT reminder_channels_pkey PRIMARY KEY (id),
    CONSTRAINT reminder_channels_reminder_id_fkey FOREIGN KEY (reminder_id) REFERENCES public.reminders (id) ON DELETE CASCADE,
    CONSTRAINT reminder_channels_unique_reminder_channel UNIQUE (reminder_id, channel)
);

CREATE INDEX IF NOT EXISTS idx_reminder_channels_reminder_id ON public.reminder_channels USING btree (reminder_id);
CREATE INDEX IF NOT EXISTS idx_reminder_channels_channel ON public.reminder_channels USING btree (channel);

-- Backfill: existing single-channel reminders → one row in reminder_channels
INSERT INTO public.reminder_channels (id, reminder_id, channel)
SELECT uuid_generate_v4(), r.id, r.channel
FROM public.reminders r
WHERE NOT EXISTS (
    SELECT 1
    FROM public.reminder_channels rc
    WHERE rc.reminder_id = r.id
);

CREATE TABLE IF NOT EXISTS public.reminder_deliveries (
    id uuid NOT NULL,
    reminder_id uuid NOT NULL,
    channel character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    attempt_count integer NOT NULL DEFAULT 0,
    last_attempt_at timestamp with time zone,
    sent_at timestamp with time zone,
    last_error text,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT reminder_deliveries_pkey PRIMARY KEY (id),
    CONSTRAINT reminder_deliveries_reminder_id_fkey FOREIGN KEY (reminder_id) REFERENCES public.reminders (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reminder_deliveries_reminder_id ON public.reminder_deliveries USING btree (reminder_id);
CREATE INDEX IF NOT EXISTS idx_reminder_deliveries_reminder_id_channel ON public.reminder_deliveries USING btree (reminder_id, channel);
