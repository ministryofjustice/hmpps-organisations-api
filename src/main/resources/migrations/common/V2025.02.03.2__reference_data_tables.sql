---------------------------------------------------------------------------------------
-- Contains coded reference values used to constrain the values of lists/validation.
-- e.g. address types, phone types, county codes, country codes, relationship types etc..
-- Still questions over reference data - who owns this? Other uses in NOMIS?
-- One-way sync from DPS to NOMIS?
-- Local maintenance of reference data? Forms/menu options? Roles to maintain?
----------------------------------------------------------------------------------------

CREATE TABLE reference_codes
(
    reference_code_id   bigserial NOT NULL CONSTRAINT reference_code_pk PRIMARY KEY,
    group_code          varchar(40) NOT NULL,
    code                varchar(40) NOT NULL,
    description         varchar(100) NOT NULL,
    display_order       integer NOT NULL,
    is_active           boolean NOT NULL,
    created_by          varchar(100) NOT NULL,
    created_time        timestamp NOT NULL DEFAULT current_timestamp,
    updated_by          varchar(100),
    updated_time        timestamp
);

CREATE UNIQUE INDEX idx_reference_code_group ON reference_codes(group_code, code);

-- End