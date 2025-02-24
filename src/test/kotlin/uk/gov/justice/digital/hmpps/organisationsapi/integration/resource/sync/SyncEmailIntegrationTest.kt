package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncEmailResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OrganisationInfo
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import java.time.LocalDateTime

@TestPropertySource(properties = ["feature.events.sns.enabled=true"])
class SyncEmailIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class EmailSyncTests {

    @BeforeEach
    fun resetEvents() {
      stubEvents.reset()
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/organisation-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/organisation-email")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateEmailRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/organisation-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateEmailRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/organisation-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/organisation-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/organisation-email")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateEmailRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/organisation-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateEmailRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/organisation-email/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should create a email address linked to an organisation`() {
      val organisationCreated = createOrganisationWithFixedId(7001L)
      val email = createEmail(organisationCreated.organisationId)

      with(email) {
        assertThat(this.organisationId).isEqualTo(organisationCreated.organisationId)
        assertThat(emailAddress).isEqualTo("created@example.com")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_EMAIL_CREATED,
        additionalInfo = OrganisationInfo(email.organisationId, email.organisationEmailId, Source.NOMIS),
      )
    }

    @Test
    fun `should update an email address linked to an organisation`() {
      val organisation = createOrganisationWithFixedId(7002L)
      val email = createEmail(organisation.organisationId)
      val updatedEmail = updateEmail(email.organisationEmailId, email.organisationId)

      with(updatedEmail) {
        assertThat(organisationEmailId).isEqualTo(email.organisationEmailId)
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(emailAddress).isEqualTo("updated@example.com")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("UPDATER")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_EMAIL_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, email.organisationEmailId, Source.NOMIS),
      )
    }

    @Test
    fun `should delete an email address linked to an organisation`() {
      val organisation = createOrganisationWithFixedId(7003L)
      val email = createEmail(organisation.organisationId)

      webTestClient.delete()
        .uri("/sync/organisation-email/{organisationEmailId}", email.organisationEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/organisation-email/{organisationEmailId}", email.organisationEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_EMAIL_DELETED,
        additionalInfo = OrganisationInfo(organisation.organisationId, email.organisationEmailId, Source.NOMIS),
      )
    }

    @Test
    fun `should get an email address by ID`() {
      val organisation = createOrganisationWithFixedId(7004L)
      val email = createEmail(organisation.organisationId)
      val emailRetrieved = getEmailById(email.organisationEmailId)

      with(emailRetrieved) {
        assertThat(organisationEmailId).isEqualTo(email.organisationEmailId)
        assertThat(this.organisationId).isEqualTo(email.organisationId)
        assertThat(emailAddress).isEqualTo("created@example.com")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }
    }

    private fun syncUpdateEmailRequest(organisationId: Long) = SyncUpdateEmailRequest(
      organisationId = organisationId,
      emailAddress = "updated@example.com",
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )

    private fun syncCreateEmailRequest(organisationId: Long) = SyncCreateEmailRequest(
      organisationId = organisationId,
      emailAddress = "created@example.com",
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
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

    private fun createEmail(organisationId: Long) =
      webTestClient.post()
        .uri("/sync/organisation-email")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncCreateEmailRequest(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncEmailResponse::class.java)
        .returnResult().responseBody!!

    private fun getEmailById(organisationEmailId: Long) =
      webTestClient.get()
        .uri("/sync/organisation-email/{organisationEmailId}", organisationEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncEmailResponse::class.java)
        .returnResult().responseBody!!

    private fun updateEmail(organisationEmailId: Long, organisationId: Long) =
      webTestClient.put()
        .uri("/sync/organisation-email/{organisationEmailId}", organisationEmailId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncUpdateEmailRequest(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncEmailResponse::class.java)
        .returnResult().responseBody!!
  }
}
