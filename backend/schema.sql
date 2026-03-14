--
-- PostgreSQL database dump
--

\restrict GRbl77agKVNGu0d1Iuch6n0jq9yQgu1h2Q44PhnLOB9PhY9O5HB9tFrg6pOVEQl

-- Dumped from database version 16.11 (Ubuntu 16.11-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.11 (Ubuntu 16.11-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: event_users; Type: TABLE; Schema: public; Owner: laytonreynolds
--

CREATE TABLE public.event_users (
    event_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.event_users OWNER TO laytonreynolds;

--
-- Name: events; Type: TABLE; Schema: public; Owner: laytonreynolds
--

CREATE TABLE public.events (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    description character varying(255),
    start_time timestamp with time zone NOT NULL,
    end_time timestamp with time zone,
    source integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    timezone text NOT NULL
);


ALTER TABLE public.events OWNER TO laytonreynolds;

--
-- Name: reminders; Type: TABLE; Schema: public; Owner: laytonreynolds
--

CREATE TABLE public.reminders (
    id uuid NOT NULL,
    event_id uuid NOT NULL,
    user_id uuid NOT NULL,
    remind_at timestamp with time zone NOT NULL,
    status character varying(255) NOT NULL,
    channel character varying(255) NOT NULL,
    created_at timestamp with time zone NOT NULL
);


ALTER TABLE public.reminders OWNER TO laytonreynolds;

--
-- Name: users; Type: TABLE; Schema: public; Owner: laytonreynolds
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    phone_number character varying(255) NOT NULL,
    timezone character varying(255) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    password_hash character varying(255) DEFAULT ''::text NOT NULL
);


ALTER TABLE public.users OWNER TO laytonreynolds;

--
-- Name: event_users event_users_pkey; Type: CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.event_users
    ADD CONSTRAINT event_users_pkey PRIMARY KEY (event_id, user_id);


--
-- Name: events events_pkey; Type: CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);


--
-- Name: reminders reminders_pkey; Type: CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.reminders
    ADD CONSTRAINT reminders_pkey PRIMARY KEY (id);


--
-- Name: users users_phone_number_key; Type: CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_event_users_event_id; Type: INDEX; Schema: public; Owner: laytonreynolds
--

CREATE INDEX idx_event_users_event_id ON public.event_users USING btree (event_id);


--
-- Name: idx_event_users_user_id; Type: INDEX; Schema: public; Owner: laytonreynolds
--

CREATE INDEX idx_event_users_user_id ON public.event_users USING btree (user_id);


--
-- Name: idx_reminder_due; Type: INDEX; Schema: public; Owner: laytonreynolds
--

CREATE INDEX idx_reminder_due ON public.reminders USING btree (remind_at, status);


--
-- Name: idx_user_phone_number; Type: INDEX; Schema: public; Owner: laytonreynolds
--

CREATE INDEX idx_user_phone_number ON public.users USING btree (phone_number);


--
-- Name: events events_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: event_users fkci0b9ys3awpour3lsn05dq8r4; Type: FK CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.event_users
    ADD CONSTRAINT fkci0b9ys3awpour3lsn05dq8r4 FOREIGN KEY (event_id) REFERENCES public.events(id);


--
-- Name: event_users fkl0jar0mnl3hqk84wwio9u8cmy; Type: FK CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.event_users
    ADD CONSTRAINT fkl0jar0mnl3hqk84wwio9u8cmy FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: reminders reminders_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.reminders
    ADD CONSTRAINT reminders_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.events(id);


--
-- Name: reminders reminders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: laytonreynolds
--

ALTER TABLE ONLY public.reminders
    ADD CONSTRAINT reminders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict GRbl77agKVNGu0d1Iuch6n0jq9yQgu1h2Q44PhnLOB9PhY9O5HB9tFrg6pOVEQl

