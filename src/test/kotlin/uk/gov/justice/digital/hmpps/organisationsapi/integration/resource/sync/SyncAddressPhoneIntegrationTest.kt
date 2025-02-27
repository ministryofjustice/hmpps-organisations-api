package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OrganisationInfo
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import java.time.LocalDate
import java.time.LocalDateTime

class SyncAddressPhoneIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class AddressPhoneSyncTests {
    @BeforeEach
    fun initialiseData() {
      stubEvents.reset()
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/organisation-address-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/organisation-address-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateAddressPhoneRequest())
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/organisation-address-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createAddressPhoneRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/organisation-address-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_ORGANISATIONS__RW", "ROLE_ORGANISATIONS__R"])
    fun `Sync endpoints should return forbidden without an authorised role on the token`(role: String) {
      webTestClient.get()
        .uri("/sync/organisation-address-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/organisation-address-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(createAddressPhoneRequest(1L))
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/organisation-address-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateAddressPhoneRequest())
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/organisation-address-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should create and get an organisation address phone number`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(createOrganisationRequest(3001L))
      val address = testAPIClient.syncCreateAnAddress(createAddressRequest(organisation.organisationId))
      val addressPhone = testAPIClient.syncCreateAnAddressPhone(createAddressPhoneRequest(address.organisationAddressId))

      val response = getAnAddressPhoneById(addressPhone.organisationAddressPhoneId)

      with(response) {
        assertThat(phoneType).isEqualTo("HOME")
        assertThat(phoneNumber).isEqualTo("12345")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_ADDRESS_PHONE_CREATED,
        additionalInfo = OrganisationInfo(
          organisationId = organisation.organisationId,
          identifier = addressPhone.organisationAddressPhoneId,
          Source.NOMIS,
        ),
      )
    }

    @Test
    fun `should create and then update an organisation address phone number`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(createOrganisationRequest(3002L))
      val address = testAPIClient.syncCreateAnAddress(createAddressRequest(organisation.organisationId))
      val addressPhone = testAPIClient.syncCreateAnAddressPhone(createAddressPhoneRequest(address.organisationAddressId))

      with(addressPhone) {
        assertThat(phoneType).isEqualTo("HOME")
        assertThat(phoneNumber).isEqualTo("12345")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      val response = getAnAddressPhoneById(addressPhone.organisationAddressPhoneId)

      with(response) {
        assertThat(phoneType).isEqualTo("HOME")
        assertThat(phoneNumber).isEqualTo("12345")
      }

      val updatedAddressPhone = updateAnAddressPhone(addressPhone.organisationAddressPhoneId, updateAddressPhoneRequest())

      with(updatedAddressPhone) {
        assertThat(phoneType).isEqualTo("MOB")
        assertThat(phoneNumber).isEqualTo("54321")
        assertThat(extNumber).isEqualTo("1")
        assertThat(updatedBy).isEqualTo("UPDATE")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isNotNull()
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_ADDRESS_PHONE_UPDATED,
        additionalInfo = OrganisationInfo(
          organisationId = organisation.organisationId,
          identifier = addressPhone.organisationAddressPhoneId,
          Source.NOMIS,
        ),
      )
    }

    @Test
    fun `should delete an existing address phone number`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(createOrganisationRequest(3003L))
      val address = testAPIClient.syncCreateAnAddress(createAddressRequest(organisation.organisationId))
      val addressPhone = testAPIClient.syncCreateAnAddressPhone(createAddressPhoneRequest(address.organisationAddressId))

      with(addressPhone) {
        assertThat(phoneType).isEqualTo("HOME")
        assertThat(phoneNumber).isEqualTo("12345")
        assertThat(createdBy).isEqualTo("CREATE")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      webTestClient.delete()
        .uri("/sync/organisation-address-phone/{organisationAddressPhoneId}", addressPhone.organisationAddressPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/organisation-address-phone/{organisationAddressPhoneId}", addressPhone.organisationAddressPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_ADDRESS_PHONE_DELETED,
        additionalInfo = OrganisationInfo(
          organisationId = organisation.organisationId,
          identifier = addressPhone.organisationAddressPhoneId,
          Source.NOMIS,
        ),
      )
    }

    private fun createAddressPhoneRequest(organisationAddressId: Long) = SyncCreateAddressPhoneRequest(
      organisationAddressId = organisationAddressId,
      phoneType = "HOME",
      phoneNumber = "12345",
      extNumber = null,
      createdBy = "CREATE",
    )

    private fun updateAddressPhoneRequest() = SyncUpdateAddressPhoneRequest(
      phoneType = "MOB",
      phoneNumber = "54321",
      extNumber = "1",
      updatedBy = "UPDATE",
      updatedTime = LocalDateTime.now(),
    )

    private fun createOrganisationRequest(organisationId: Long) = SyncCreateOrganisationRequest(
      organisationId = organisationId,
      organisationName = "Organisation123",
      programmeNumber = "PRG123",
      vatNumber = "VAT123",
      caseloadId = "HEI",
      comments = "comment123",
      active = true,
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun createAddressRequest(organisationId: Long) = SyncCreateAddressRequest(
      organisationId = organisationId,
      addressType = "HOME",
      primaryAddress = true,
      mailAddress = true,
      serviceAddress = false,
      noFixedAddress = false,
      property = "82",
      street = "The Street",
      area = "The Area",
      postcode = "A12 4AA",
      contactPersonName = "created contact",
      comments = "created comment",
      startDate = LocalDate.now().minusDays(10),
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun createPhoneRequest(organisationId: Long) = SyncCreatePhoneRequest(
      organisationId = organisationId,
      phoneType = "MOB",
      phoneNumber = "07999 123456",
      extNumber = null,
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun getAnAddressPhoneById(organisationAddressPhoneId: Long) = webTestClient.get()
      .uri("/sync/organisation-address-phone/{organisationAddressPhoneId}", organisationAddressPhoneId)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncAddressPhoneResponse::class.java)
      .returnResult().responseBody!!

    private fun updateAnAddressPhone(
      organisationAddressPhoneId: Long,
      request: SyncUpdateAddressPhoneRequest,
    ) = webTestClient.put()
      .uri("/sync/organisation-address-phone/{organisationAddressPhoneId}", organisationAddressPhoneId)
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncAddressPhoneResponse::class.java)
      .returnResult().responseBody!!
  }
}
