package uk.gov.justice.digital.hmpps.organisationsapi.integration

import org.junit.jupiter.api.Test

class NotFoundTest : PostgresIntegrationTestBase() {

  @Test
  fun `Resources that aren't found should return 404 - test of the exception handler`() {
    webTestClient.get().uri("/some-url-not-found")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isNotFound
  }
}
