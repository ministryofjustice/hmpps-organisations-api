package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OrganisationInfo
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDateTime

@TestPropertySource(properties = ["feature.events.sns.enabled=true"])
class SyncOrganisationsIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class OrganisationSyncTests {

    @BeforeEach
    fun resetEvents() {
      stubEvents.reset()
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/organisation/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/organisation")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateOrganisationRequest(5000L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/organisation/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateOrganisationRequest(5000L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/organisation/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/organisation/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/organisation")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateOrganisationRequest(5000L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/organisation/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateOrganisationRequest(5000L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/organisation/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should create and then get an organisation by ID`() {
      val organisationCreated = createOrganisationWithFixedId(5001L)
      val organisation = getOrganisationById(organisationCreated.organisationId)

      with(organisation) {
        assertThat(this.organisationId).isEqualTo(organisationId)
        assertThat(organisationName).isEqualTo("Organisation123")
        assertThat(programmeNumber).isEqualTo("PRG123")
        assertThat(vatNumber).isEqualTo("VAT123")
        assertThat(caseloadId).isEqualTo("HEI")
        assertThat(comments).isEqualTo("comment123")
        assertThat(active).isEqualTo(true)
        assertThat(deactivatedDate).isNull()
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_CREATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )
    }

    @Test
    fun `should create a new organisation with fixed ID`() {
      val organisation = createOrganisationWithFixedId(5002L)
      with(organisation) {
        assertThat(this.organisationId).isEqualTo(5002L)
        assertThat(organisationName).isEqualTo("Organisation123")
        assertThat(programmeNumber).isEqualTo("PRG123")
        assertThat(vatNumber).isEqualTo("VAT123")
        assertThat(caseloadId).isEqualTo("HEI")
        assertThat(comments).isEqualTo("comment123")
        assertThat(active).isEqualTo(true)
        assertThat(deactivatedDate).isNull()
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_CREATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )
    }

    @Test
    fun `should create and then update an organisation`() {
      val organisation = createOrganisationWithFixedId(5003L)
      with(organisation) {
        assertThat(this.organisationId).isEqualTo(5003L)
        assertThat(organisationName).isEqualTo("Organisation123")
      }

      val updated = updateOrganisation(organisation.organisationId)
      with(updated) {
        assertThat(this.organisationId).isEqualTo(5003L)
        assertThat(organisationName).isEqualTo("Organisation321")
        assertThat(programmeNumber).isEqualTo("PRG321")
        assertThat(vatNumber).isEqualTo("VAT321")
        assertThat(comments).isEqualTo("comment321")
        assertThat(caseloadId).isEqualTo("AGI")
        assertThat(active).isEqualTo(true)
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("UPDATER")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_CREATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_UPDATED,
        additionalInfo = OrganisationInfo(updated.organisationId, updated.organisationId, Source.NOMIS),
      )
    }

    @Test
    fun `should create and then delete an organisation`() {
      val organisation = createOrganisationWithFixedId(5004L)
      with(organisation) {
        assertThat(this.organisationId).isEqualTo(5004L)
        assertThat(organisationName).isEqualTo("Organisation123")
      }

      webTestClient.delete()
        .uri("/sync/organisation/{organisationId}", organisation.organisationId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/organisations/{organisationId}", organisation.organisationId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_CREATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_DELETED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )
    }

    @Test
    fun `should report a conflict when creating an organisation ID that already exists`() {
      val organisation = createOrganisationWithFixedId(5005L)
      with(organisation) {
        assertThat(this.organisationId).isEqualTo(5005L)
        assertThat(organisationName).isEqualTo("Organisation123")
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_CREATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )

      resetEvents()

      val expectedError = webTestClient.post()
        .uri("/sync/organisation")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncCreateOrganisationRequest(organisation.organisationId))
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(ErrorResponse::class.java)
        .returnResult().responseBody!!

      assertThat(expectedError.status).isEqualTo(HttpStatus.CONFLICT.value())
      assertThat(expectedError.userMessage).isEqualTo("Sync: Duplicate organisation ID received 5005")

      stubEvents.assertHasNoEvents(OutboundEvent.ORGANISATION_CREATED)
    }

    private fun syncUpdateOrganisationRequest(organisationId: Long) = SyncUpdateOrganisationRequest(
      organisationId = organisationId,
      organisationName = "Organisation321",
      programmeNumber = "PRG321",
      vatNumber = "VAT321",
      caseloadId = "AGI",
      comments = "comment321",
      active = true,
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )

    private fun syncCreateOrganisationRequest(organisationId: Long) = SyncCreateOrganisationRequest(
      // Sync creates supply a fixed ID from NOMIS (i.e. the corporate ID)
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

    private fun createOrganisationWithFixedId(organisationId: Long) =
      webTestClient.post()
        .uri("/sync/organisation")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncCreateOrganisationRequest(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncOrganisationResponse::class.java)
        .returnResult().responseBody!!

    private fun getOrganisationById(organisationId: Long) =
      webTestClient.get()
        .uri("/sync/organisation/{organisationId}", organisationId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncOrganisationResponse::class.java)
        .returnResult().responseBody!!

    private fun updateOrganisation(organisationId: Long) =
      webTestClient.put()
        .uri("/sync/organisation/{organisationId}", organisationId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncUpdateOrganisationRequest(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncOrganisationResponse::class.java)
        .returnResult().responseBody!!
  }
}
