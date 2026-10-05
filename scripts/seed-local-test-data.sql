-- Seed linked organisation test data into the local organisations database.
-- Change org_count below to control how many organisations are created.
-- Run from the repository root:
-- docker compose exec -T db psql -v ON_ERROR_STOP=1 -U organisations -d organisations-db < scripts/seed-local-test-data.sql
-- Each organisation gets types, a global phone, an address-specific phone,
-- email, web address, and two addresses with address-phone links.

BEGIN;

DO $seed$
DECLARE
  org_count integer := 10;
  i integer;
  org_id bigint;
  address_id bigint;
  phone_id bigint;
BEGIN
  IF current_database() <> 'organisations-db' THEN
    RAISE EXCEPTION 'Refusing to seed database "%"; expected local database "organisations-db"', current_database();
  END IF;

  IF org_count < 1 THEN
    RAISE EXCEPTION 'org_count must be at least 1';
  END IF;

  FOR i IN 1..org_count LOOP
    INSERT INTO organisation (
      organisation_name,
      programme_number,
      vat_number,
      caseload_id,
      comments,
      active,
      deactivated_date,
      created_by
    )
    VALUES (
      'Local Test Organisation ' || lpad(i::text, 3, '0'),
      'LOCAL-PROG-' || i,
      'GB' || lpad(i::text, 9, '0'),
      'BXI',
      'Generated local test data',
      true,
      NULL,
      'LOCAL_TEST_SEED'
    )
    RETURNING organisation_id INTO org_id;

    INSERT INTO organisation_type (organisation_id, organisation_type, created_by)
    VALUES
      (org_id, 'TRUST', 'LOCAL_TEST_SEED'),
      (org_id, 'PROG', 'LOCAL_TEST_SEED');

    INSERT INTO organisation_phone (
      organisation_id, phone_type, phone_number, ext_number, created_by
    )
    VALUES (
      org_id, 'BUS', '+44 20 7946 ' || lpad((1000 + i)::text, 4, '0'), NULL, 'LOCAL_TEST_SEED'
    );

    INSERT INTO organisation_email (organisation_id, email_address, created_by)
    VALUES (
      org_id, 'contact' || i || '@local-test.example', 'LOCAL_TEST_SEED'
    );

    INSERT INTO organisation_web_address (organisation_id, web_address, created_by)
    VALUES (
      org_id, 'https://organisation-' || i || '.local-test.example', 'LOCAL_TEST_SEED'
    );

    INSERT INTO organisation_address (
      organisation_id, address_type, primary_address, mail_address, service_address,
      flat, property, street, area, city_code, county_code, post_code, country_code,
      special_needs_code, contact_person_name, business_hours, start_date, end_date,
      no_fixed_address, comments, created_by
    )
    VALUES (
      org_id, 'BUS', true, true, true,
      NULL, 'Test House ' || i, 'Example Street', 'Test Area', NULL, NULL,
      'SW1A 1AA', 'ENG', NULL, 'Test Contact', 'Monday-Friday 09:00-17:00',
      CURRENT_DATE, NULL, false, 'Primary business address', 'LOCAL_TEST_SEED'
    )
    RETURNING organisation_address_id INTO address_id;

    INSERT INTO organisation_phone (
      organisation_id, phone_type, phone_number, ext_number, created_by
    )
    VALUES (
      org_id, 'BUS', '+44 20 7946 ' || lpad((2000 + i)::text, 4, '0'), '123', 'LOCAL_TEST_SEED'
    )
    RETURNING organisation_phone_id INTO phone_id;

    INSERT INTO organisation_address_phone (
      organisation_id, organisation_address_id, organisation_phone_id, created_by
    )
    VALUES (org_id, address_id, phone_id, 'LOCAL_TEST_SEED');

    INSERT INTO organisation_address (
      organisation_id, address_type, primary_address, mail_address, service_address,
      flat, property, street, area, city_code, county_code, post_code, country_code,
      special_needs_code, contact_person_name, business_hours, start_date, end_date,
      no_fixed_address, comments, created_by
    )
    VALUES (
      org_id, 'WORK', false, false, false,
      NULL, 'Office ' || i, 'Another Example Road', 'Other Test Area', NULL, NULL,
      'M1 1AA', 'ENG', 'DEAF', 'Second Test Contact', 'Monday-Friday 10:00-16:00',
      CURRENT_DATE, NULL, false, 'Secondary address', 'LOCAL_TEST_SEED'
    )
    RETURNING organisation_address_id INTO address_id;

    INSERT INTO organisation_phone (
      organisation_id, phone_type, phone_number, ext_number, created_by
    )
    VALUES (
      org_id, 'FAX', '+44 20 7946 ' || lpad((3000 + i)::text, 4, '0'), NULL, 'LOCAL_TEST_SEED'
    )
    RETURNING organisation_phone_id INTO phone_id;

    INSERT INTO organisation_address_phone (
      organisation_id, organisation_address_id, organisation_phone_id, created_by
    )
    VALUES (org_id, address_id, phone_id, 'LOCAL_TEST_SEED');
  END LOOP;

  RAISE NOTICE 'Created % local test organisations and linked details', org_count;
END
$seed$;

COMMIT;

-- Verify seeded organisations and linked data:
SELECT
  COUNT(DISTINCT o.organisation_id) AS organisations,
  COUNT(DISTINCT ot.organisation_id) AS organisations_with_types,
  COUNT(DISTINCT oa.organisation_address_id) AS addresses,
  COUNT(DISTINCT op.organisation_phone_id) AS phone_numbers,
  COUNT(DISTINCT oap.organisation_address_phone_id) AS address_phone_links,
  COUNT(DISTINCT oe.organisation_email_id) AS email_addresses,
  COUNT(DISTINCT ow.organisation_web_address_id) AS web_addresses
FROM organisation o
LEFT JOIN organisation_type ot ON ot.organisation_id = o.organisation_id
LEFT JOIN organisation_address oa ON oa.organisation_id = o.organisation_id
LEFT JOIN organisation_phone op ON op.organisation_id = o.organisation_id
LEFT JOIN organisation_address_phone oap ON oap.organisation_id = o.organisation_id
LEFT JOIN organisation_email oe ON oe.organisation_id = o.organisation_id
LEFT JOIN organisation_web_address ow ON ow.organisation_id = o.organisation_id
WHERE o.created_by = 'LOCAL_TEST_SEED';
