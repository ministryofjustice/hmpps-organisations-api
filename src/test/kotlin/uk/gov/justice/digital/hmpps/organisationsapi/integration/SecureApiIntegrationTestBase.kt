package uk.gov.justice.digital.hmpps.organisationsapi.integration

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.test.web.reactive.server.WebTestClient

abstract class SecureApiIntegrationTestBase : PostgresIntegrationTestBase() {

  private val allPossibleRoles = setOf(
    "ROLE_ORGANISATIONS_MIGRATION",
    "ROLE_ORGANISATIONS__R",
    "ROLE_ORGANISATIONS__RW",
    "ROLE_WRONG",
  )

  @Test
  fun `should return unauthorized if no token`() {
    baseRequestBuilder()
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    baseRequestBuilder()
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @TestFactory
  fun `should return forbidden for roles without access`(): Iterable<DynamicTest> {
    val forbiddenRoles = allPossibleRoles - allowedRoles
    return forbiddenRoles.map { forbiddenRole ->
      DynamicTest.dynamicTest("Requests with role ($forbiddenRole) should be forbidden") {
        baseRequestBuilder()
          .headers(setAuthorisation(roles = listOf(forbiddenRole)))
          .exchange()
          .expectStatus()
          .isForbidden
      }
    }
  }

  abstract fun baseRequestBuilder(): WebTestClient.RequestHeadersSpec<*>

  abstract val allowedRoles: Set<String>
}
