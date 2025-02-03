# hmpps-organisations-api

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-organisations-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-organisations-api "Link to report")
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-organisations-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-organisations-api-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

API to support the creation, management and sharing of data related to organisations external to HMPPS.


# Building the project

```bash
./gradlew
./gradlew clean test
```

## Running the application locally

```bash
./run-local.sh
```

The application comes with a `dev` spring profile that includes default settings for running locally. This is not
necessary when deploying to kubernetes as these values are included in the helm configuration templates -
e.g. `values-dev.yaml`.

Most services which run locally will rely upon the DEV environment for dependant APIs, but will need a database
running locally.  This can be started with:

```bash
docker compose pull && docker compose up -d
```
