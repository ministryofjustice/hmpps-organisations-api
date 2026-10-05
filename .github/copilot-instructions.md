# Repository instructions

## Build, test, and lint

- Use JDK 25 or later and the Gradle wrapper.
- Build and run the full test suite: `./gradlew clean build`
- Run tests only: `./gradlew test`
- Run one test class: `./gradlew test --tests 'uk.gov.justice.digital.hmpps.organisationsapi.service.OrganisationServiceTest'`
- Check Kotlin formatting: `./gradlew ktlintCheck`; apply formatting with `./gradlew ktlintFormat`.
- The CI pipeline also lints the Helm chart for dev, preprod, and prod. For example: `helm lint helm_deploy/hmpps-organisations-api -f helm_deploy/values-dev.yaml`.
- For local execution, configure the environment variables documented in `README.md`, start dependencies with `docker compose up -d`, then run `./run-local.sh`.

## Architecture

This is a Kotlin Spring Boot API backed by PostgreSQL. HTTP controllers in `resource/` handle routing, validation, authorization, and API documentation; they call facades, which coordinate services and cross-cutting effects. Services implement domain operations using Spring Data repositories, while `mapping/` extension functions convert between request/response models and JPA entities.

The organisation domain is relational: an organisation has separate rows for types, addresses, phones, emails, and web addresses, with address-phone links. Full detail responses are assembled from these repositories. Summary and search responses use the `v_organisation_summary` database view, mapped by `OrganisationSummaryEntity`; keep changes to that read model aligned across its Flyway view migration, entity, repository, and mapper.

There are two write paths. Standard endpoints create DPS organisations with database-generated IDs (20,000,000 and above). The `/sync` endpoints serve NOMIS synchronisation and use `OrganisationWithFixedIdEntity` to preserve NOMIS IDs (below 20,000,000); sync updates replace the supplied organisation fields rather than applying a partial patch. `SyncFacade` publishes outbound events with source `NOMIS`, while standard API operations use the DPS source. Preserve this distinction so downstream sync consumers can avoid processing their own changes.

Database schema and reference data are managed by Flyway under `src/main/resources/migrations/`: shared migrations are in `common/`, with environment-specific migrations in their respective locations. Versioned `V...` files change schema/data; repeatable `R__...` files define views.

## Repository conventions

- Keep controllers thin: use `@Valid` on request models, `@PreAuthorize` for role checks, and delegate business work through the facade/service boundary. API errors are translated centrally in `OrganisationsApiExceptionHandler`.
- Keep transport models, persistence entities, and conversions separate. Follow the existing `toModel`/`toEntity` extension-mapper pattern rather than embedding serialization logic in controllers or entities.
- When changing organisation fields shared by persistence variants, check `OrganisationEntity`, `BaseOrganisationEntity`, and `OrganisationWithFixedIdEntity` together.
- Treat `/sync` `PUT` requests as full updates, not PATCH operations. Preserve the incoming NOMIS IDs and the `NOMIS` event source for sync changes.
- Add database changes as new Flyway migrations; do not rewrite migrations that may already have run. Update the summary view migration when search/summary fields or selection rules change.
- Unit tests live alongside the domain package structure under `src/test/kotlin`. Integration tests use Spring Boot, JWT/auth and WireMock extensions; PostgreSQL-backed cases use `PostgresIntegrationTestBase` and Testcontainers. Follow the relevant existing base class when adding coverage.
