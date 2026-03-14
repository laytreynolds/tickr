CREATE TABLE public.event_users (
    event_id uuid NOT NULL,
    user_id uuid NOT NULL
);

--
CREATE TABLE public.events (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    description character varying(255),
    start_time timestamp
    with
        time zone NOT NULL,
        end_time timestamp
    with
        time zone,
        source integer NOT NULL,
        created_at timestamp
    with
        time zone NOT NULL,
        timezone text NOT NULL
);

CREATE TABLE public.reminders (
    id uuid NOT NULL,
    event_id uuid NOT NULL,
    user_id uuid NOT NULL,
    remind_at timestamp
    with
        time zone NOT NULL,
        status character varying(255) NOT NULL,
        channel character varying(255) NOT NULL,
        created_at timestamp
    with
        time zone NOT NULL
);

CREATE TABLE public.users (
    id uuid NOT NULL,
    phone_number character varying(255) NOT NULL,
    timezone character varying(255) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    password_hash character varying(255) DEFAULT ''::text NOT NULL
);

ALTER TABLE ONLY public.event_users
ADD CONSTRAINT event_users_pkey PRIMARY KEY (event_id, user_id);

--
--

ALTER TABLE ONLY public.events
ADD CONSTRAINT events_pkey PRIMARY KEY (id);

--
--

ALTER TABLE ONLY public.reminders
ADD CONSTRAINT reminders_pkey PRIMARY KEY (id);

--
--

ALTER TABLE ONLY public.users
ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);

ALTER TABLE ONLY public.users
ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
CREATE INDEX idx_event_users_event_id ON public.event_users USING btree (event_id);

--
CREATE INDEX idx_event_users_user_id ON public.event_users USING btree (user_id);

--
CREATE INDEX idx_reminder_due ON public.reminders USING btree (remind_at, status);

--
CREATE INDEX idx_user_phone_number ON public.users USING btree (phone_number);

--
ALTER TABLE ONLY public.events
ADD CONSTRAINT events_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id);

--
ALTER TABLE ONLY public.event_users
ADD CONSTRAINT fkci0b9ys3awpour3lsn05dq8r4 FOREIGN KEY (event_id) REFERENCES public.events (id);

--
ALTER TABLE ONLY public.event_users
ADD CONSTRAINT fkl0jar0mnl3hqk84wwio9u8cmy FOREIGN KEY (user_id) REFERENCES public.users (id);

--
ALTER TABLE ONLY public.reminders
ADD CONSTRAINT reminders_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events (id);

--
ALTER TABLE ONLY public.reminders
ADD CONSTRAINT reminders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id);

--
-- PostgreSQL database dump complete
--

\unrestrict GRbl77agKVNGu0d1Iuch6n0jq9yQgu1h2Q44PhnLOB9PhY9O5HB9tFrg6pOVEQl