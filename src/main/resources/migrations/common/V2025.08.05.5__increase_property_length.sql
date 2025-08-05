DROP VIEW IF EXISTS v_organisation_addresses;
DROP VIEW IF EXISTS v_organisation_summary;

ALTER TABLE organisation_address ALTER COLUMN property TYPE VARCHAR(130);