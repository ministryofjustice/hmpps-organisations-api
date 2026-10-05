# hmpps-organisations-api

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-organisations-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-organisations-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-organisations-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://organisations-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html#/)

API to support the creation, management and sharing of data related to organisations which are external to HMPPS.
These organisations are referred to as CORPORATES within NOMIS.


# Building the project

Tools required locally

* JDK v25+
* Kotlin plugin (Intellij)
* docker
* docker-compose

## Installing gradle

```bash
./gradlew
./gradlew clean build
```

## Running the service

There are two key environment variables needed to run the service. The system client id and secret used to retrieve the OAuth 2.0 access token needed for "service to service" API calls can be set as local environment variables.
This allows API calls made from this service that do not use the caller's token to successfully authenticate.

Add the following to a local `.env` file in the root folder of this project (_you can extract the credentials from the dev k8s project namespace_).

N.B. you must escape any '$' characters with '\\$'


| var                    | description                                                                                         | example value    |
|------------------------|-----------------------------------------------------------------------------------------------------|------------------|
| SYSTEM_CLIENT_ID       | system client id used for auth                                                                      | <no_example>     |
| SYSTEM_CLIENT_SECRET   | system client secret used for auth                                                                  | <no_example>     |
| DB_SERVER              | the host of the local DB (used by application.yaml <br/> and application-test.yaml)                 | localhost        |
| DB_NAME                | the name of the database (for local, <br/> it relates to the name docker compose uses)              | organisations-db |
| DB_USER                | the username for the database (for local, <br/> it relates to the user docker compose uses)         | organisations    |
| DB_PASS                | the password for the database (for local, <br/> it relates to the user docker compose uses)         | organisations    |
| DB_SSL_MODE            | the security of the database connection                                                             | prefer           |
| LOCAL_DB_PORT          | the port of of the database (for local, <br/> it relates to the user docker compose uses)           | 5772             |
| POSTGRES_TEST_DB_PORT  | the port the integration tests use (optional, set if you don't want it using the default 5432 port) | 5773             |
| DPR_USER               | <>                                                                                                  | dpr_user         |
| DPR_PASSWORD           | <>                                                                                                  | dpr_password     |

To build and start Postgres and the API together with Docker Compose, run this from the repository root:

```
docker compose up --build -d
```

Postgres is available on `localhost:${LOCAL_DB_PORT:-5432}` and the API on
<http://localhost:8080>. The API container waits for the database health check before starting.
When the `local` profile is active, the API grants local developer roles to requests without requiring
a bearer token. This bypass is only for local development; do not activate the `local` profile outside
your machine. For calls that require outbound service-to-service authentication, set `SYSTEM_CLIENT_ID`
and `SYSTEM_CLIENT_SECRET` in `.env` to valid credentials; local placeholder values are used otherwise.
Prison names are not looked up from Prison Register locally, so `caseloadPrisonName` is null in local responses.
To stop both services, run `docker compose down`.

Alternatively, start only Postgres with Docker Compose and run the API directly on your machine using:

```
docker compose up -d db
./run-local.sh
```

or you can use the `Run API Locally` run config and point it to your custom .env file, which should be automatically picked up in intellij but is located in .run if you need to add it manually

# Running the test suite

```bash
./gradlew test
```

# Gradle tasks

To see the full list of tasks available:

```bash
./gradlew tasks
```
