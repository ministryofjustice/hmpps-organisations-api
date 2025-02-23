package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncWebResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OrganisationInfo
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import java.time.LocalDateTime

@TestPropertySource(properties = ["feature.events.sns.enabled=true"])
class SyncWebIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class WebSyncTests {

    @BeforeEach
    fun resetEvents() {
      stubEvents.reset()
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/organisation-web/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.post()
        .uri("/sync/organisation-web")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateWebRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/organisation-web/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateWebRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.delete()
        .uri("/sync/organisation-web/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/organisation-web/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.post()
        .uri("/sync/organisation-web")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncCreateWebRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/organisation-web/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateWebRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.delete()
        .uri("/sync/organisation-web/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should create a web address linked to an organisation`() {
      val organisationCreated = createOrganisationWithFixedId(8001L)
      val web = createWeb(organisationCreated.organisationId)

      with(web) {
        assertThat(this.organisationId).isEqualTo(organisationCreated.organisationId)
        assertThat(webAddress).isEqualTo("www.created.com")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_WEB_CREATED,
        additionalInfo = OrganisationInfo(web.organisationId, web.organisationWebAddressId, Source.NOMIS),
      )
    }

    @Test
    fun `should update a web address linked to an organisation`() {
      val organisation = createOrganisationWithFixedId(8002L)
      val web = createWeb(organisation.organisationId)
      val updatedWeb = updateWeb(web.organisationWebAddressId, web.organisationId)

      with(updatedWeb) {
        assertThat(organisationWebAddressId).isEqualTo(web.organisationWebAddressId)
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(webAddress).isEqualTo("www.updated.com")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isEqualTo("UPDATER")
        assertThat(updatedTime).isAfter(LocalDateTime.now().minusMinutes(5))
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_WEB_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, web.organisationWebAddressId, Source.NOMIS),
      )
    }

    @Test
    fun `should delete a web address linked to an organisation`() {
      val organisation = createOrganisationWithFixedId(8003L)
      val web = createWeb(organisation.organisationId)

      webTestClient.delete()
        .uri("/sync/organisation-web/{organisationWebId}", web.organisationWebAddressId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/sync/organisation-web/{organisationWebId}", web.organisationWebAddressId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isNotFound

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_WEB_DELETED,
        additionalInfo = OrganisationInfo(organisation.organisationId, web.organisationWebAddressId, Source.NOMIS),
      )
    }

    @Test
    fun `should get a web address by ID`() {
      val organisation = createOrganisationWithFixedId(8004L)
      val web = createWeb(organisation.organisationId)
      val webRetrieved = getWebById(web.organisationWebAddressId)

      with(webRetrieved) {
        assertThat(organisationWebAddressId).isEqualTo(web.organisationWebAddressId)
        assertThat(this.organisationId).isEqualTo(web.organisationId)
        assertThat(webAddress).isEqualTo("www.created.com")
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(createdTime).isAfter(LocalDateTime.now().minusMinutes(5))
        assertThat(updatedBy).isNull()
        assertThat(updatedTime).isNull()
      }
    }

    private fun syncUpdateWebRequest(organisationId: Long) = SyncUpdateWebRequest(
      organisationId = organisationId,
      webAddress = "www.updated.com",
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )

    private fun syncCreateWebRequest(organisationId: Long) = SyncCreateWebRequest(
      organisationId = organisationId,
      webAddress = "www.created.com",
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

    private fun createWeb(organisationId: Long) =
      webTestClient.post()
        .uri("/sync/organisation-web")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncCreateWebRequest(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncWebResponse::class.java)
        .returnResult().responseBody!!

    private fun getWebById(organisationWebId: Long) =
      webTestClient.get()
        .uri("/sync/organisation-web/{organisationWebId}", organisationWebId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncWebResponse::class.java)
        .returnResult().responseBody!!

    private fun updateWeb(organisationWebId: Long, organisationId: Long) =
      webTestClient.put()
        .uri("/sync/organisation-web/{organisationWebId}", organisationWebId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncUpdateWebRequest(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncWebResponse::class.java)
        .returnResult().responseBody!!
  }
}
