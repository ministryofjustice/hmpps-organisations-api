# Manage Organisations — Discovery & Scope v0.1

**HMPPS Move & Improve | Squad 5 | Working draft**

## Summary

The organisations API and its database already exist. This work appears to be about enabling users to create an organisation in DPS and have the corresponding record created in NOMIS. The repo contains pieces of that flow, but does not prove the end-to-end integration is working. Confirm it before building a duplicate.

## What already exists

- **NOMIS → DPS:** the API has migration and sync endpoints for organisations and related data (types, addresses, phone numbers, emails and web addresses), plus ID reconciliation. This is implemented and integration-tested in this repo; verify it is operating as required in the target environment.
- **DPS create API:** `POST /organisation` stores core organisation fields in the API database and generates a DPS ID.
- **Create event:** the API publishes an `ORGANISATION_CREATED` event marked `source: DPS`; the deployment chart enables it. A code comment says the external Syscon sync service acts on DPS-sourced events.
- **Important limitation:** the create endpoint is deprecated and says it does not currently synchronise to NOMIS. Syscon is outside this repo, so we cannot confirm that it consumes the event and creates the NOMIS record. The endpoint only accepts core fields; related-data lists are empty on creation. Event publish errors are logged and swallowed.
- **No UI here:** this repository contains no DPS screen for creating an organisation. There is also no standard business-user organisation update endpoint in the main controller.

**Likely flow to verify:** `DPS screen → organisations API/database → DPS domain event → Syscon → NOMIS`. The API and NOMIS appear to maintain separate records; confirm how their IDs are linked and how failures are handled.

## What needs doing

1. **Trace the existing create path.** In a non-production environment, create an organisation via the API and verify whether Syscon creates it in NOMIS, how the IDs are mapped, and what happens on event or NOMIS failure. Check required fields and whether related data is in scope.
2. **Agree the minimum create journey.** Confirm users, required fields, validation, duplicate handling, permissions, and whether create-only is the first release or whether editing/deactivation is also required.
3. **Build only the confirmed gaps.** Reuse the existing create endpoint/event if they meet the agreed requirements. Otherwise update the API/event contract and/or Syscon integration. Add a DPS screen that submits the agreed data and communicates pending, success and failure states.
4. **Prove the full flow.** Test create, ID mapping, read-back from both systems, duplicates, retries and recovery. Agree who supports records that fail to sync.

## Provisional sample tickets

These are draft tickets, not final scope. Ticket 1 should determine which implementation tickets are actually needed.

### 1. Verify DPS-to-NOMIS organisation creation

Trace `POST /organisation` through its DPS event and the deployed Syscon integration to NOMIS in a non-production environment.

**Acceptance criteria**
- Confirm whether Syscon consumes the event and creates a NOMIS corporate record.
- Document DPS-to-NOMIS ID mapping and how each system can read the resulting record.
- Verify required fields, duplicate delivery, publish failure, NOMIS failure, retry and recovery behaviour.
- Record the actual gaps and owning team; recommend API, integration, UI or no further backend changes.

### 2. Agree organisation creation requirements

Agree the first-release journey with users and operational/NOMIS SMEs.

**Acceptance criteria**
- Confirm users, permissions, required fields, validation, reference data and duplicate handling.
- Decide whether addresses, types and contact details are needed at creation.
- Decide whether editing/deactivation is in scope.
- Define what users see while NOMIS creation is pending or has failed.

### 3. Complete the API / integration gaps (if needed)

Update the existing API and/or Syscon path so an authorised DPS create results in a usable NOMIS record.

**Acceptance criteria**
- Reuse `POST /organisation` and `ORGANISATION_CREATED` where suitable; avoid duplicate contracts.
- Support agreed data and reliable, observable, recoverable event processing.
- Handle ID mapping and duplicate/retried requests safely.
- Add automated tests and remove the create endpoint's deprecation only when the end-to-end path is verified.

### 4. Build the DPS create screen

Let authorised users enter and submit the agreed organisation data without starting the task in NOMIS.

**Acceptance criteria**
- Use the agreed fields, guidance, validation, permissions and duplicate-check behaviour.
- Show clear pending, success and recoverable failure states consistent with NOMIS processing.
- Cover successful submission, validation, duplicates and integration failures in UI tests.

### 5. Verify readiness and hand over support

Demonstrate the end-to-end flow and agree operational ownership before release.

**Acceptance criteria**
- Verify create, ID mapping and read-back in both systems.
- Document monitoring, reconciliation, recovery steps and support ownership.
- Agree release evidence that users can complete the in-scope create task without NOMIS.

## Evidence in this repo

- `src/main/kotlin/uk/gov/justice/digital/hmpps/organisationsapi/resource/OrganisationController.kt` — create, search and read routes.
- `src/main/kotlin/uk/gov/justice/digital/hmpps/organisationsapi/facade/OrganisationFacade.kt` — create event publication.
- `src/main/kotlin/uk/gov/justice/digital/hmpps/organisationsapi/facade/SyncFacade.kt` and `resource/sync/` — NOMIS-originated sync routes and events.
- `src/main/kotlin/uk/gov/justice/digital/hmpps/organisationsapi/service/events/OutboundEventsService.kt` — event publishing and failure handling.
- `src/main/kotlin/uk/gov/justice/digital/hmpps/organisationsapi/resource/migrate/` — migration endpoint and service.
- `helm_deploy/hmpps-organisations-api/values.yaml` — event feature configuration.

**Conclusion:** NOMIS-to-DPS sync capability is present in this API. The key unknown is whether the existing DPS event is handled end-to-end by Syscon to create the NOMIS record. Verify that first; then build the DPS screen and only the backend/integration pieces shown to be missing.
