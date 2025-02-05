--
-- Tables, indexes and reference data required for an organisation and a contacts employment with an organisation.
--

---------------------------------------------------------------------------------------
-- Organisations have several small pieces of metadata associated with them.
-- This table stores that metadata and has a unique primary id range to avoid collision with CORPORATE_ID in NOMIS.
----------------------------------------------------------------------------------------

CREATE TABLE organisation
(
    organisation_id bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 20000000) PRIMARY KEY,
    organisation_name varchar(40) NOT NULL,
    programme_number varchar(40), -- FEI_NUMBER in NOMIS
    vat_number varchar(12),
    caseload_id varchar(6),
    comments varchar(240),
    active boolean NOT NULL,    -- added
    deactivated_date date,      -- added
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    updated_time timestamp
);

CREATE INDEX idx_organisation_organisation_name ON organisation(organisation_name);
CREATE INDEX idx_organisation_created_time ON organisation(created_time);

--
-- Add GIN index for organisation search
--
CREATE INDEX idx_organisation_name_gin ON organisation USING gin (organisation_name gin_trgm_ops);

---------------------------------------------------------------------------------------
-- Organisations may have multiple types.
-- This table stores the types of an organisation.
----------------------------------------------------------------------------------------

CREATE TABLE organisation_type(
    organisation_id bigint NOT NULL REFERENCES organisation(organisation_id),
    organisation_type varchar(12) NOT NULL, -- Reference codes - ORGANISATION_TYPE
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    updated_time timestamp
);

ALTER TABLE organisation_type ADD CONSTRAINT organisation_type_pkey PRIMARY KEY (organisation_id, organisation_type);

CREATE INDEX idx_organisation_type_organisation_id ON organisation_type(organisation_id);
CREATE INDEX idx_organisation_type_created_time ON organisation_type(created_time);

---------------------------------------------------------------------------------------
-- Organisations may have multiple telephone numbers.
-- This table stores the telephone numbers related to an organisation.
----------------------------------------------------------------------------------------

CREATE TABLE organisation_phone
(
    organisation_phone_id bigserial NOT NULL CONSTRAINT organisation_phone_id_pk PRIMARY KEY,
    organisation_id bigint NOT NULL REFERENCES organisation(organisation_id),
    phone_type varchar(12) NOT NULL, -- Reference codes - PHONE_TYPE
    phone_number varchar(40) NOT NULL,
    ext_number varchar(7),
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    updated_time timestamp
);

CREATE INDEX idx_organisation_phone_organisation_id ON organisation_phone(organisation_id);
CREATE INDEX idx_organisation_phone_number ON organisation_phone(phone_number);
CREATE INDEX idx_organisation_phone_created_time ON organisation_phone(created_time);

---------------------------------------------------------------------------------------
-- Organisations may have multiple email addresses.
-- This table stores the email addresses related to an organisation.
----------------------------------------------------------------------------------------

CREATE TABLE organisation_email
(
    organisation_email_id bigserial NOT NULL CONSTRAINT organisation_email_id_pk PRIMARY KEY,
    organisation_id bigint NOT NULL REFERENCES organisation(organisation_id),
    email_address varchar(240) NOT NULL,
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    updated_time timestamp
);

CREATE INDEX idx_organisation_email_organisation_id ON organisation_email(organisation_id);
CREATE INDEX idx_organisation_email_address ON organisation_email(email_address);

---------------------------------------------------------------------------------------
-- Organisations may have multiple web addresses.
-- This table stores the web addresses related to an organisation.
----------------------------------------------------------------------------------------

CREATE TABLE organisation_web_address
(
    organisation_web_address_id bigserial NOT NULL CONSTRAINT organisation_web_address_id_pk PRIMARY KEY,
    organisation_id bigint NOT NULL REFERENCES organisation(organisation_id),
    web_address varchar(240) NOT NULL,
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    updated_time timestamp
);

CREATE INDEX idx_organisation_web_address_organisation_id ON organisation_web_address(organisation_id);
CREATE INDEX idx_organisation_web_address_web_address ON organisation_web_address(web_address);


---------------------------------------------------------------------------------------
-- Organisations may have one or more addresses.
-- This table holds the details of addresses
----------------------------------------------------------------------------------------

CREATE TABLE organisation_address
(
    organisation_address_id bigserial NOT NULL CONSTRAINT organisation_address_id_pk PRIMARY KEY,
    organisation_id bigint NOT NULL REFERENCES organisation(organisation_id),
    address_type varchar(12), -- Reference code - ADDRESS_TYPE e.g. HOME, WORK
    primary_address boolean NOT NULL DEFAULT false,
    mail_address boolean NOT NULL DEFAULT false,
    service_address boolean NOT NULL DEFAULT false,
    flat varchar(30),
    property varchar(50),
    street varchar(160),
    area varchar(70),
    city_code varchar(12), -- Reference code - CITY
    county_code varchar(12), -- Reference code - COUNTY
    post_code varchar(12),
    country_code varchar(12), -- Reference code - COUNTRY
    special_needs_code varchar(12),	-- Reference code - ORG_ADDRESS_SPECIAL_NEEDS
    contact_person_name	varchar(40),
    business_hours varchar(60),
    start_date date,
    end_date date,
    no_fixed_address boolean NOT NULL DEFAULT false,
    comments varchar(240),
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    updated_time timestamp
);

CREATE INDEX idx_organisation_address_organisation_id ON organisation_address(organisation_id);

---------------------------------------------------------------
-- Address-specific phone numbers for organisations.
-- Currently modelled as a join-table between organisation_phone and organisation_address.
----------------------------------------------------------------------------------------

CREATE TABLE organisation_address_phone
(
    organisation_address_phone_id bigserial NOT NULL CONSTRAINT organisation_address_phone_id_pk PRIMARY KEY,
    organisation_id bigint NOT NULL REFERENCES organisation(organisation_id),
    organisation_address_id bigint NOT NULL REFERENCES organisation_address(organisation_address_id),
    organisation_phone_id bigint NOT NULL REFERENCES organisation_phone(organisation_phone_id),
    created_by varchar(100) NOT NULL,
    created_time timestamp NOT NULL DEFAULT current_timestamp,
    updated_by varchar(100),
    updated_time timestamp
);

CREATE INDEX idx_organisation_address_phone_organisation_id ON organisation_address_phone(organisation_id);
CREATE INDEX idx_organisation_address_phone_organisation_address_id ON organisation_address_phone(organisation_address_id);
CREATE INDEX idx_organisation_address_phone_organisation_phone_id ON organisation_address_phone(organisation_phone_id);

-- End