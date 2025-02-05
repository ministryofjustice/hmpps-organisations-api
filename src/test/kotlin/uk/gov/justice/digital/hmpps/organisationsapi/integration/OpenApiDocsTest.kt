package uk.gov.justice.digital.hmpps.organisationsapi.integration

import io.swagger.v3.parser.OpenAPIV3Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenApiDocsTest : PostgresIntegrationTestBase() {
  @LocalServerPort
  private val port: Int = 0

  @Test
  fun `open api docs are available`() {
    webTestClient.get()
      .uri("/swagger-ui/index.html?configUrl=/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `open api docs redirect to correct page`() {
    webTestClient.get()
      .uri("/swagger-ui.html")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is3xxRedirection
      .expectHeader().value("Location") { it.contains("/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config") }
  }

  @Test
  fun `the open api json contains server documentation`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("servers").isNotEmpty
  }

  @Test
  fun `the open api json contains the version number`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("info.version").isEqualTo(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
  }

  @Test
  fun `the open api json is valid and contains documentation`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)
    assertThat(result.messages).isEmpty()
    assertThat(result.openAPI.paths).isNotEmpty()
  }

  @ParameterizedTest
  @CsvSource(value = ["bearer-jwt"])
  fun `the security scheme is setup for bearer tokens`(key: String) {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.securitySchemes.$key.type").isEqualTo("http")
      .jsonPath("$.components.securitySchemes.$key.scheme").isEqualTo("bearer")
      .jsonPath("$.components.securitySchemes.$key.bearerFormat").isEqualTo("JWT")
  }

  @Test
  fun `all endpoints have a security scheme defined`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.paths[*][*][?(!@.security)]").doesNotExist()
  }
}
