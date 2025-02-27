--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
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
-- Create database if not exists
--
DO $$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_database WHERE datname = 'card_test'
   ) THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE card_test');
   END IF;
END $$;

--
-- Name: cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cards (
    name character varying(255) NOT NULL,
    description character varying(255),
    color character varying(255),
    status character varying(255) DEFAULT 'To Do'::character varying,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    creator character varying(255) NOT NULL,
    is_active character(1) DEFAULT 'Y'::bpchar,
    CONSTRAINT cards_is_active_check CHECK ((is_active = ANY (ARRAY['Y'::bpchar, 'N'::bpchar]))),
    CONSTRAINT status_check CHECK (((status)::text = ANY (ARRAY[('To Do'::character varying)::text, ('In Progress'::character varying)::text, ('Done'::character varying)::text])))
);


ALTER TABLE public.cards OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY (ARRAY[('Member'::character varying)::text, ('Admin'::character varying)::text])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: cards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cards (name, description, color, status, date_created, creator, is_active) FROM stdin;
CardOne			To Do	2025-02-15 17:43:57.823473	adminone@administrators.com	Y
CardTwo_deleted_2025-02-15T18:40:15.781901100	Currently being processed.	#000000	In Progress	2025-02-15 18:09:50.010639	adminone@administrators.com	N
CardFourMember_deleted_2025-02-15T18:50:32.151308400			To Do	2025-02-15 18:45:25.425532	memberthree@allmembers.com	N
CardFiveMember	Currently being processed.	#000000	In Progress	2025-02-18 07:45:35.300308	memberfour@allmembers.com	Y
CardSix_deleted_2025-02-18T08:15:51.281621500	\N	\N	To Do	2025-02-18 07:49:40.96573	memberfour@allmembers.com	N
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (email, password, role, date_created) FROM stdin;
adminone@administrators.com	$2a$10$5M44l1L2MPnM1bP7bH3dLuiEVkgkkx6m7il.TRNkoAuxMRxrmWmxm	Admin	2025-02-13 11:47:54.60513
memberone@allmembers.com	$2a$10$axVkI2YaJWHsO6ZowODvauj2/F0SQJozz1QK91mHKCq/rcyTBW0ga	Member	2025-02-13 11:55:42.605248
membertwo@allmembers.com	$2a$10$WcnRRT9J1JNFkqK5TcZdW.ub4kyFt8R4avOOKnYGsZs1a0cLu6pje	Member	2025-02-15 16:26:08.650672
memberthree@allmembers.com	$2a$10$FX/AYqrAB24jSOuka4u7Z.4L38mYPzHow41dIEK3kjCeDP8zpycTi	Member	2025-02-15 16:45:02.998876
memberfour@allmembers.com	$2a$10$Hav1/g4gLo87Oupt7AYyUeXxj0pGGJMz9ZC0MOdLZm6yN2L17SDN6	Member	2025-02-18 07:00:48.445192
memberfive@allmembers.com	$2a$10$fcNUqpVRiigJLYeYbXt7Fud0rKTxNGSB6Bgc4WKjYfEQ32OYvTyGC	Member	2025-02-18 07:24:54.511915
\.


--
-- Name: cards cards_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cards
    ADD CONSTRAINT cards_pkey PRIMARY KEY (name);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (email);


--
-- PostgreSQL database dump complete
--

