package uk.gov.justice.digital.hmpps.organisationsapi.integration.database

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase

class MigrationsApplyTest : PostgresIntegrationTestBase() {

  @Test
  fun `should apply all migrations and start the service using a real postgres`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
      .jsonPath("components.hmppsAuth.status").isEqualTo("UP")
  }
}
