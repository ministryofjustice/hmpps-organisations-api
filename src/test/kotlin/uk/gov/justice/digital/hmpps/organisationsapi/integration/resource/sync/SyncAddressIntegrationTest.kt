package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OrganisationInfo
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import java.time.LocalDate
import java.time.LocalDateTime

@TestPropertySource(properties = ["feature.events.sns.enabled=true"])
class SyncAddressIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class AddressSyncTests {

    @BeforeEach
    fun resetEvents() {
      stubEvents.reset()
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/organisation-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/organisation-address")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateAddressRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/organisation-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateAddressRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/organisation-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/organisation-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/organisation-address")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateAddressRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/organisation-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateAddressRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/organisation-address/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should create an address linked to an organisation`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(9001L))
      val address = testAPIClient.syncCreateAnAddress(syncCreateAddressRequest(organisation.organisationId))

      with(address) {
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(organisationAddressId).isNotNull()
        assertThat(addressType).isEqualTo("HOME")
        assertThat(primaryAddress).isTrue()
        assertThat(mailAddress).isTrue()
        assertThat(serviceAddress).isFalse()
        assertThat(noFixedAddress).isFalse()
        assertThat(property).isEqualTo("82")
        assertThat(street).isEqualTo("The Street")
        assertThat(area).isEqualTo("The Area")
        assertThat(postcode).isEqualTo("A12 4AA")
        assertThat(contactPersonName).isEqualTo("created contact")
        assertThat(comments).isEqualTo("created comment")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_ADDRESS_CREATED,
        additionalInfo = OrganisationInfo(address.organisationId, address.organisationAddressId, Source.NOMIS),
      )
    }

    @Test
    fun `should update an address linked to an organisation`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(9002L))
      val address = testAPIClient.syncCreateAnAddress(syncCreateAddressRequest(organisation.organisationId))
      val updatedAddress = updateAddress(address.organisationAddressId, address.organisationId)

      with(updatedAddress) {
        assertThat(organisationAddressId).isEqualTo(address.organisationAddressId)
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(addressType).isEqualTo("BUS")
        assertThat(primaryAddress).isFalse()
        assertThat(mailAddress).isFalse()
        assertThat(serviceAddress).isFalse()
        assertThat(noFixedAddress).isFalse()
        assertThat(property).isEqualTo("82")
        assertThat(street).isEqualTo("The Street")
        assertThat(area).isEqualTo("The Area")
        assertThat(postcode).isEqualTo("A12 4AA")
        assertThat(contactPersonName).isEqualTo("updated contact")
        assertThat(comments).isEqualTo("updated comment")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("UPDATER")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_ADDRESS_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, address.organisationAddressId, Source.NOMIS),
      )
    }

    @Test
    fun `should delete an address linked to an organisation`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(9003L))
      val address = testAPIClient.syncCreateAnAddress(syncCreateAddressRequest(organisation.organisationId))

      webTestClient.delete()
        .uri("/sync/organisation-address/{organisationAddressId}", address.organisationAddressId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/organisation-address/{organisationAddressId}", address.organisationAddressId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_ADDRESS_DELETED,
        additionalInfo = OrganisationInfo(organisation.organisationId, address.organisationAddressId, Source.NOMIS),
      )
    }

    @Test
    fun `should get a address by ID`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(9004L))
      val address = testAPIClient.syncCreateAnAddress(syncCreateAddressRequest(organisation.organisationId))
      val addressRetrieved = getAddressById(address.organisationAddressId)

      with(addressRetrieved) {
        assertThat(organisationAddressId).isEqualTo(address.organisationAddressId)
        assertThat(organisationId).isEqualTo(address.organisationId)
        assertThat(addressType).isEqualTo(address.addressType)
        assertThat(postcode).isEqualTo(address.postcode)
        assertThat(contactPersonName).isEqualTo(address.contactPersonName)
        assertThat(comments).isEqualTo(address.comments)
        assertThat(createdBy).isEqualTo(address.createdBy)
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }
    }

    private fun syncUpdateAddressRequest(organisationId: Long) = SyncUpdateAddressRequest(
      organisationId = organisationId,
      addressType = "BUS",
      primaryAddress = false,
      mailAddress = false,
      serviceAddress = false,
      noFixedAddress = false,
      property = "82",
      street = "The Street",
      area = "The Area",
      postcode = "A12 4AA",
      contactPersonName = "updated contact",
      comments = "updated comment",
      startDate = LocalDate.now().minusDays(10),
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )

    private fun syncCreateAddressRequest(organisationId: Long) = SyncCreateAddressRequest(
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

    private fun syncCreateOrganisationRequest(organisationId: Long) = SyncCreateOrganisationRequest(
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

    private fun getAddressById(organisationAddressId: Long) = webTestClient.get()
      .uri("/sync/organisation-address/{organisationAddressId}", organisationAddressId)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncAddressResponse::class.java)
      .returnResult().responseBody!!

    private fun updateAddress(organisationAddressId: Long, organisationId: Long) = webTestClient.put()
      .uri("/sync/organisation-address/{organisationAddressId}", organisationAddressId)
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .bodyValue(syncUpdateAddressRequest(organisationId))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncAddressResponse::class.java)
      .returnResult().responseBody!!
  }
}
