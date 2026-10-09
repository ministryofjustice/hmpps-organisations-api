package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.organisationsapi.integration.SecureApiIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2AddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2CreateRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2InternetAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2PhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationV2Details
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate
import java.time.LocalDateTime

class CreateOrganisationIntegrationTest : SecureApiIntegrationTestBase() {

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  override val allowedRoles: Set<String> = setOf("ROLE_ORGANISATIONS__RW")

  override fun baseRequestBuilder(): WebTestClient.RequestHeadersSpec<*> = webTestClient.post()
    .uri("/organisation")
    .bodyValue(createValidOrganisationRequest())

  @Test
  fun `should return bad request when organisation name exceeds 40 characters`() {
    val request = createValidOrganisationRequest().copy(
      organisationName = "A".repeat(41),
    )

    val errors = webTestClient.post()
      .uri("/organisation")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): organisationName must be <= 40 characters")
  }

  @Test
  fun `should return bad request when programme number exceeds 40 characters`() {
    val request = createValidOrganisationRequest().copy(
      programmeNumber = "A".repeat(41),
    )

    val errors = webTestClient.post()
      .uri("/organisation")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): programmeNumber must be <= 40 characters")
  }

  @Test
  fun `should return bad request when VAT number exceeds 12 characters`() {
    val request = createValidOrganisationRequest().copy(
      vatNumber = "A".repeat(13),
    )

    val errors = webTestClient.post()
      .uri("/organisation")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): vatNumber must be <= 12 characters")
  }

  @Test
  fun `should return bad request when caseload ID exceeds 6 characters`() {
    val request = createValidOrganisationRequest().copy(
      caseloadId = "A".repeat(7),
    )

    val errors = webTestClient.post()
      .uri("/organisation")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): caseloadId must be <= 6 characters")
  }

  @Test
  fun `should return bad request when comments exceed 240 characters`() {
    val request = createValidOrganisationRequest().copy(
      comments = "A".repeat(241),
    )

    val errors = webTestClient.post()
      .uri("/organisation")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(errors.userMessage).isEqualTo("Validation failure(s): comments must be <= 240 characters")
  }

  @Test
  fun `should return bad request when required fields are missing`() {
    val request = mapOf(
      "active" to true,
      "createdTime" to LocalDateTime.now(),
    )

    webTestClient.post()
      .uri("/organisation")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `should create organisation successfully with valid data`() {
    val request = createValidOrganisationRequest()

    stubPrisonRegisterGetNamesById("TEST1", "Test Prison")

    val response = webTestClient.post()
      .uri("/organisation")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectBody(OrganisationDetails::class.java)
      .returnResult()
      .responseBody

    assertThat(response).isNotNull
    with(response!!) {
      assertThat(organisationName).isEqualTo(request.organisationName)
      assertThat(programmeNumber).isEqualTo(request.programmeNumber)
      assertThat(vatNumber).isEqualTo(request.vatNumber)
      assertThat(caseloadId).isEqualTo(request.caseloadId)
      assertThat(caseloadPrisonName).isEqualTo("Test Prison")
      assertThat(comments).isEqualTo(request.comments)
      assertThat(active).isEqualTo(request.active)
      assertThat(deactivatedDate).isEqualTo(request.deactivatedDate)
      assertThat(createdBy).isEqualTo(request.createdBy)
      assertThat(createdTime).isNotNull()
      assertThat(organisationId).isNotNull()
    }
  }

  @Test
  fun `should create organisation with all v2 details and persist them`() {
    val request = createValidV2OrganisationRequest()

    val response = webTestClient.post()
      .uri("/organisation/v2")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectBody(OrganisationV2Details::class.java)
      .returnResult()
      .responseBody!!

    assertThat(response).isEqualTo(OrganisationV2Details.from(response.organisationId, request))
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_type where organisation_id = ?",
        Int::class.java,
        response.organisationId,
      ),
    ).isEqualTo(1)
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_phone where organisation_id = ?",
        Int::class.java,
        response.organisationId,
      ),
    ).isEqualTo(2)
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_email where organisation_id = ?",
        Int::class.java,
        response.organisationId,
      ),
    ).isEqualTo(1)
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_web_address where organisation_id = ?",
        Int::class.java,
        response.organisationId,
      ),
    ).isEqualTo(1)
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_address where organisation_id = ? and active = true",
        Int::class.java,
        response.organisationId,
      ),
    ).isEqualTo(1)
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_address_phone where organisation_id = ?",
        Int::class.java,
        response.organisationId,
      ),
    ).isEqualTo(1)
  }

  @Test
  fun `should replace organisation and children on v2 put`() {
    val request = createValidV2OrganisationRequest()
    val created = webTestClient.post()
      .uri("/organisation/v2")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isCreated
      .expectBody(OrganisationV2Details::class.java)
      .returnResult()
      .responseBody!!

    val updateRequest = request.copy(
      organisationName = "Renamed Organisation",
      addresses = emptyList(),
      internetAddresses = emptyList(),
      updatedBy = "editor",
      updatedTime = LocalDateTime.now(),
    )
    webTestClient.put()
      .uri("/organisation/v2/${created.organisationId}")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(updateRequest)
      .exchange()
      .expectStatus()
      .isOk

    assertThat(
      jdbcTemplate.queryForObject(
        "select organisation_name from organisation where organisation_id = ?",
        String::class.java,
        created.organisationId,
      ),
    ).isEqualTo("Renamed Organisation")
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_address where organisation_id = ?",
        Int::class.java,
        created.organisationId,
      ),
    ).isEqualTo(0)
    assertThat(
      jdbcTemplate.queryForObject(
        "select count(*) from organisation_phone where organisation_id = ?",
        Int::class.java,
        created.organisationId,
      ),
    ).isEqualTo(1)
  }

  @Test
  fun `should return not found on v2 put for unknown organisation`() {
    webTestClient.put()
      .uri("/organisation/v2/999999999")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(createValidV2OrganisationRequest())
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should reject v2 request with invalid reference code`() {
    webTestClient.post()
      .uri("/organisation/v2")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(createValidV2OrganisationRequest().copy(organisationTypes = listOf("NOPE")))
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `should reject v2 request with two primary addresses`() {
    val request = createValidV2OrganisationRequest()
    webTestClient.post()
      .uri("/organisation/v2")
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
      .bodyValue(request.copy(addresses = request.addresses + request.addresses))
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `should reject v2 request with blank name or end date before start date`() {
    val request = createValidV2OrganisationRequest()
    listOf(
      request.copy(organisationName = " "),
      request.copy(addresses = listOf(request.addresses[0].copy(endDate = LocalDate.of(2015, 1, 1)))),
    ).forEach {
      webTestClient.post()
        .uri("/organisation/v2")
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__RW")))
        .bodyValue(it)
        .exchange()
        .expectStatus()
        .isBadRequest
    }
  }

  companion object {
    private fun createValidV2OrganisationRequest() = OrganisationV2CreateRequest(
      organisationName = "V2 Test Organisation",
      programmeNumber = "FEI-12345",
      vatNumber = "GB123456789",
      caseloadId = null,
      comments = "Legacy comments",
      organisationTypes = listOf("OTH"),
      active = true,
      deactivatedDate = null,
      phoneNumbers = listOf(
        OrganisationV2PhoneRequest("BUS", "01632 960123", null),
      ),
      internetAddresses = listOf(
        OrganisationV2InternetAddressRequest("EMAIL", "contact@example.org"),
        OrganisationV2InternetAddressRequest("WEB", "https://www.example.org"),
      ),
      addresses = listOf(
        OrganisationV2AddressRequest(
          noFixedAddress = false,
          flat = "Flat 1",
          property = "Example House",
          street = "Example Street",
          area = "Example Locality",
          cityCode = "25343",
          countyCode = "MANCHESTER",
          postCode = "A3 6TO",
          countryCode = "ENG",
          startDate = LocalDate.of(2016, 4, 1),
          endDate = null,
          comments = "Address comments",
          addressType = "BUS",
          serviceAddress = true,
          specialNeedsCode = null,
          primaryAddress = true,
          mailAddress = true,
          contactPersonName = "Example Contact",
          businessHours = "Weekdays 9-5",
          phoneNumbers = listOf(OrganisationV2PhoneRequest("BUS", "01632 960124", "42")),
        ),
      ),
      createdBy = "test-user",
      createdTime = LocalDateTime.now(),
      updatedBy = null,
      updatedTime = null,
    )

    fun createValidOrganisationRequest() = CreateOrganisationRequest(
      organisationName = "Test Organisation",
      programmeNumber = "TEST01",
      vatNumber = "GB123456789",
      caseloadId = "TEST1",
      comments = "Test comments",
      active = true,
      deactivatedDate = null,
      createdBy = "test-user",
      createdTime = LocalDateTime.now(),
      updatedBy = null,
      updatedTime = null,
    )
  }
}
