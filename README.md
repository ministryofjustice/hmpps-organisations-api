# hmpps-organisations-api

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-organisations-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-organisations-api "Link to report")
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-organisations-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://organisations-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html#/)

API to support the creation, management and sharing of data related to organisations which are external to HMPPS.
These organisations are referred to as CORPORATES within NOMIS.


# Building the project

Tools required locally

* JDK v21+
* Kotlin plugin (Intellij)
* docker
* docker-compose

## Installing gradle

```bash
./gradlew
./gradlew clean build
```

## Running the application locally

There are two environment variables which need to be supplied locally.
The most common way is to create a file called .env and to set the two values within it.
These values should be obtained from the development team.

### Set environment variables

```bash
SYSTEM_CLIENT_ID=<system.client.id>
SYSTEM_CLIENT_SECRET=<system.client.secret>
```

### Run a docker Postsgresql database container

```bash
docker-compose pull && docker-compose up -d
```
This will download and run a docker Postgresql database within your docker environment and make it available
on localhost:5432 to used.

### Run the application

Provided you have the environment variables set correctly, and access to the development
environment APIs (via VPN), you can start the application with:

```bash
./run-local.sh
```

# Running the test suite

```bash
./gradlew test
```

# Gradle tasks

To see the full list of tasks available:

```bash
./gradlew tasks
```
