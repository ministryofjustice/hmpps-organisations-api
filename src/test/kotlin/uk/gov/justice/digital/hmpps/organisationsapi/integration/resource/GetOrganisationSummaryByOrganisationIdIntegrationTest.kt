package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource

import org.apache.commons.lang3.RandomUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.organisationsapi.integration.SecureApiIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.AbstractAuditable
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationAddress
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationEmailAddress
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationPhoneNumber
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationType
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationWebAddress
import java.time.LocalDate
import java.time.LocalDateTime

@Nested
class GetOrganisationSummaryByOrganisationIdIntegrationTest : SecureApiIntegrationTestBase() {

  override val allowedRoles: Set<String> = setOf("ROLE_ORGANISATIONS__R", "ROLE_ORGANISATIONS__RW")

  override fun baseRequestBuilder(): WebTestClient.RequestHeadersSpec<*> = webTestClient.get()
    .uri("/organisation/001/summary")

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_ORGANISATIONS__R", "ROLE_ORGANISATIONS__RW"])
  fun `should return not found if no organisation found`(role: String) {
    webTestClient.get()
      .uri("/organisation/9999/summary")
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `should return organisation summary data when using a valid organisation id`() {
    val organisationId = RandomUtils.secure().randomLong()
    val migrate = MigrateOrganisationRequest(
      nomisCorporateId = organisationId,
      organisationName = "Basic Org",
      programmeNumber = "Programme Number",
      vatNumber = "VAT Number",
      caseloadId = "CL",
      comments = "Some comments",
      active = false,
      deactivatedDate = LocalDate.of(2025, 1, 2),
      organisationTypes = listOf(MigrateOrganisationType("BSKILLS").setCreatedAndModified()),
      phoneNumbers = listOf(
        MigrateOrganisationPhoneNumber(
          nomisPhoneId = RandomUtils.secure().randomLong(),
          type = "MOB",
          number = "123",
          extension = "321",
        ).setCreatedAndModified(),
      ),
      emailAddresses = listOf(
        MigrateOrganisationEmailAddress(
          nomisEmailAddressId = RandomUtils.secure().randomLong(),
          email = "test@example.com",
        ).setCreatedAndModified(),
      ),
      webAddresses = listOf(
        MigrateOrganisationWebAddress(
          nomisWebAddressId = RandomUtils.secure().randomLong(),
          webAddress = "www.example.com",
        ).setCreatedAndModified(),
      ),
      addresses = listOf(
        MigrateOrganisationAddress(
          nomisAddressId = RandomUtils.secure().randomLong(),
          type = "BUS",
          primaryAddress = true,
          mailAddress = true,
          serviceAddress = true,
          noFixedAddress = false,
          flat = "F",
          premise = "10",
          street = "Dublin Road",
          locality = "locality",
          city = "25343",
          county = "S.YORKSHIRE",
          country = "ENG",
          postCode = "D1 1DN",
          specialNeedsCode = "DEAF",
          contactPersonName = "Jeff",
          businessHours = "9-5",
          comment = "Comments",
          startDate = LocalDate.of(2020, 2, 3),
          endDate = LocalDate.of(2021, 3, 4),
          phoneNumbers = listOf(
            MigrateOrganisationPhoneNumber(
              nomisPhoneId = RandomUtils.secure().randomLong(),
              type = "ALTB",
              number = "9999999999",
              extension = "11111",
            ).setCreatedAndModified(),
            MigrateOrganisationPhoneNumber(
              nomisPhoneId = RandomUtils.secure().randomLong(),
              type = "BUS",
              number = "9123",
              extension = "321",
            ).setCreatedAndModified(),
          ),
        ).setCreatedAndModified(),
      ),
    ).setCreatedAndModified()

    testAPIClient.migrateAnOrganisation(migrate)

    val summary = testAPIClient.getOrganisationSummary(organisationId)

    with(summary) {
      assertThat(this.organisationId).isEqualTo(organisationId)
      assertThat(organisationName).isEqualTo("Basic Org")
      assertThat(organisationActive).isEqualTo(false)
      assertThat(flat).isEqualTo("F")
      assertThat(property).isEqualTo("10")
      assertThat(street).isEqualTo("Dublin Road")
      assertThat(area).isEqualTo("locality")
      assertThat(postcode).isEqualTo("D1 1DN")
      assertThat(cityCode).isEqualTo("25343")
      assertThat(cityDescription).isEqualTo("Sheffield")
      assertThat(countyCode).isEqualTo("S.YORKSHIRE")
      assertThat(countyDescription).isEqualTo("South Yorkshire")
      assertThat(countryCode).isEqualTo("ENG")
      assertThat(countryDescription).isEqualTo("England")
      assertThat(businessPhoneNumber).isEqualTo("9123")
      assertThat(businessPhoneNumberExtension).isEqualTo("321")
    }
  }

  private fun <T : AbstractAuditable> T.setCreatedAndModified(): T = apply {
    createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
    createUsername = "CREATED"
    modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
    modifyUsername = "MODIFIED"
  }
}
