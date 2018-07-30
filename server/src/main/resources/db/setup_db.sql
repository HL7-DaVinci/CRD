DROP TABLE IF EXISTS coverage_requirement_rules;

CREATE TABLE IF NOT EXISTS coverage_requirement_rules (
    id bigint NOT NULL,
    age_range_low integer NOT NULL,
    age_range_high integer NOT NULL,
    gender_code character(1),
    equipment_code character varying(255) NOT NULL,
    no_auth_needed boolean NOT NULL,
    info_link character varying(2000)
);

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY coverage_requirement_rules DROP CONSTRAINT IF EXISTS coverage_requirement_rules_pkey;
ALTER TABLE ONLY coverage_requirement_rules ADD CONSTRAINT coverage_requirement_rules_pkey PRIMARY KEY (id);
