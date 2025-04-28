package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OrganisationInfo
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import java.time.LocalDateTime

@TestPropertySource(properties = ["feature.events.sns.enabled=true"])
class SyncPhoneIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class PhoneSyncTests {

    @BeforeEach
    fun resetEvents() {
      stubEvents.reset()
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/organisation-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/organisation-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreatePhoneRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/organisation-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdatePhoneRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/organisation-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/organisation-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/organisation-phone")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreatePhoneRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/organisation-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdatePhoneRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/organisation-phone/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should create a phone number linked to an organisation`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(6001L))
      val phone = testAPIClient.syncCreateAPhone(syncCreatePhoneRequest(organisation.organisationId))

      with(phone) {
        assertThat(this.organisationId).isEqualTo(organisation.organisationId)
        assertThat(phoneType).isEqualTo("MOB")
        assertThat(phoneNumber).isEqualTo("07999 123456")
        assertThat(extNumber).isNull()
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_PHONE_CREATED,
        additionalInfo = OrganisationInfo(phone.organisationId, phone.organisationPhoneId, Source.NOMIS),
      )
    }

    @Test
    fun `should update a phone number linked to an organisation`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(6002L))
      val phone = testAPIClient.syncCreateAPhone(syncCreatePhoneRequest(organisation.organisationId))

      val updatedPhone = updatePhone(phone.organisationPhoneId, phone.organisationId)

      with(updatedPhone) {
        assertThat(organisationPhoneId).isEqualTo(phone.organisationPhoneId)
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(phoneType).isEqualTo("HOME")
        assertThat(phoneNumber).isEqualTo("07999 654321")
        assertThat(extNumber).isEqualTo("3")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("UPDATER")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_PHONE_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, phone.organisationPhoneId, Source.NOMIS),
      )
    }

    @Test
    fun `should delete a phone number linked to an organisation`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(6003L))
      val phone = testAPIClient.syncCreateAPhone(syncCreatePhoneRequest(organisation.organisationId))

      webTestClient.delete()
        .uri("/sync/organisation-phone/{organisationPhoneId}", phone.organisationPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/organisation-phone/{organisationPhoneId}", phone.organisationPhoneId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_PHONE_DELETED,
        additionalInfo = OrganisationInfo(organisation.organisationId, phone.organisationPhoneId, Source.NOMIS),
      )
    }

    @Test
    fun `should get a phone number by ID`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(6004L))
      val phone = testAPIClient.syncCreateAPhone(syncCreatePhoneRequest(organisation.organisationId))

      val phoneRetrieved = getPhoneById(phone.organisationPhoneId)

      with(phoneRetrieved) {
        assertThat(organisationPhoneId).isEqualTo(phone.organisationPhoneId)
        assertThat(this.organisationId).isEqualTo(phone.organisationId)
        assertThat(phoneType).isEqualTo("MOB")
        assertThat(phoneNumber).isEqualTo("07999 123456")
        assertThat(extNumber).isNull()
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }
    }

    private fun syncUpdatePhoneRequest(organisationId: Long) = SyncUpdatePhoneRequest(
      organisationId = organisationId,
      phoneType = "HOME",
      phoneNumber = "07999 654321",
      extNumber = "3",
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )

    private fun syncCreatePhoneRequest(organisationId: Long) = SyncCreatePhoneRequest(
      organisationId = organisationId,
      phoneType = "MOB",
      phoneNumber = "07999 123456",
      extNumber = null,
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

    private fun getPhoneById(organisationPhoneId: Long) = webTestClient.get()
      .uri("/sync/organisation-phone/{organisationPhoneId}", organisationPhoneId)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncPhoneResponse::class.java)
      .returnResult().responseBody!!

    private fun updatePhone(organisationPhoneId: Long, organisationId: Long) = webTestClient.put()
      .uri("/sync/organisation-phone/{organisationPhoneId}", organisationPhoneId)
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .bodyValue(syncUpdatePhoneRequest(organisationId))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncPhoneResponse::class.java)
      .returnResult().responseBody!!
  }
}
