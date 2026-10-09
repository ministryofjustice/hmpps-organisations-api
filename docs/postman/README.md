# Trying the v2 organisation endpoints with Postman

`organisations-v2.postman_collection.json` in this folder contains ready-made requests for the v2 create/replace endpoints and the existing read endpoints. Import it via Postman → Import.

## Run the API locally

1. `docker compose up -d`
2. `./run-local.sh`

If you run the API through docker compose, rebuild the image after code changes (`docker compose up -d --build api`); a stale image answers `Request method 'POST' is not supported` for `/organisation/v2`.

The API listens on `http://localhost:8080`. With the `local` profile active no bearer token is needed (see `LocalSecurityConfiguration`). Never use that profile outside local development.

## Requests

The collection variable `baseUrl` defaults to `http://localhost:8080`. `organisationId` is set automatically from the POST response.

| Request | Endpoint | Notes |
|---|---|---|
| Create | `POST /organisation/v2` | Creates the organisation with types, phones, emails/web addresses, addresses and address phones. Responds `201`. Needs `ROLE_ORGANISATIONS__RW`. |
| Replace | `PUT /organisation/v2/{organisationId}` | Full replace: child records omitted from the body are deleted. Responds `200`, or `404` if the organisation does not exist. Needs `ROLE_ORGANISATIONS__RW`. |
| Get detail | `GET /organisation/{organisationId}` | Existing endpoint; shows what was actually persisted. |
| Get summary | `GET /organisation/{organisationId}/summary` | Existing endpoint. |

## Request rules (400 if broken)

- `organisationName` and `createdBy` must not be blank; `organisationTypes` must not contain duplicates.
- Reference codes (organisation type, phone type, address type, city, county, country, special needs) must exist and be active. Codes that work locally: type `OTH`, phone/address type `BUS`, city `25343`, county `MANCHESTER`, country `ENG`.
- Phone numbers allow digits, spaces and `( ) + -`; extensions are digits only (max 7).
- `internetAddresses[].type` is `EMAIL` or `WEB`.
- At most one primary and one mail address; an address `endDate` cannot be before `startDate`.
- `deactivatedDate` is only allowed when `active` is `false`.
- Address `active` is derived from `endDate` and is not supplied by the client.
- The response currently echoes the request plus the generated `organisationId`; use the GET to see persisted data.
