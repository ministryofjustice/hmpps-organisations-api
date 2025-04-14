package uk.gov.justice.digital.hmpps.organisationsapi.integration.health

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuthMockServer
import uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock.PrisonRegisterApiExtension.Companion.prisonRegisterMockServer

class HealthCheckTest : PostgresIntegrationTestBase() {

  @Test
  fun `Health page reports ok`() {
    hmppsAuthMockServer.stubHealthPing(200)
    prisonRegisterMockServer.stubHealthPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down if hmpps-auth down`() {
    hmppsAuthMockServer.stubHealthPing(503)
    prisonRegisterMockServer.stubHealthPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("status").isEqualTo("DOWN")
      .jsonPath("components.hmppsAuth.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health page reports up even if prison-register-api down`() {
    prisonRegisterMockServer.stubHealthPing(503)
    hmppsAuthMockServer.stubHealthPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health ping page is accessible`() {
    webTestClient.get()
      .uri("/health/ping")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `readiness reports ok`() {
    webTestClient.get()
      .uri("/health/readiness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `liveness reports ok`() {
    webTestClient.get()
      .uri("/health/liveness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }
}
