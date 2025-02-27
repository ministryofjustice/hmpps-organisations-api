package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.sync

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncOrganisationType
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateTypesRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncTypesResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OrganisationInfo
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import java.time.LocalDateTime

@TestPropertySource(properties = ["feature.events.sns.enabled=true"])
class SyncTypesIntegrationTest : PostgresIntegrationTestBase() {

  @Nested
  inner class TypesSyncTests {

    @BeforeEach
    fun resetEvents() {
      stubEvents.reset()
    }

    @Test
    fun `Sync endpoints should return unauthorized if no token provided`() {
      webTestClient.get()
        .uri("/sync/organisation-types/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized

      webTestClient.put()
        .uri("/sync/organisation-types/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateTypesRequest(1L))
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Sync endpoints should return forbidden without an authorised role on the token`() {
      webTestClient.get()
        .uri("/sync/organisation-types/1")
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden

      webTestClient.put()
        .uri("/sync/organisation-types/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(syncUpdateTypesRequest(1L))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should update the types linked to an organisation`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(4001L))

      val response = updateTypes(organisation.organisationId)

      with(response) {
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(types).hasSize(2)
        assertThat(types).extracting("type", "createdBy", "updatedBy").containsExactlyInAnyOrder(
          Tuple("A", "CREATOR", null),
          Tuple("B", "CREATOR", "UPDATER"),
        )
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_TYPES_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )
    }

    @Test
    fun `should get an empty list of types when none exist for an organisation ID`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(4002L))

      val response = getTypesByOrganisationId(4002L)

      with(response) {
        assertThat(this.organisationId).isEqualTo(organisation.organisationId)
        assertThat(this.types).isEmpty()
      }
    }

    @Test
    fun `should get a list of types associated to an organisation by organisation ID`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(4003L))

      updateTypes(organisation.organisationId)

      val response = getTypesByOrganisationId(organisation.organisationId)

      with(response) {
        assertThat(this.organisationId).isEqualTo(organisation.organisationId)
        assertThat(this.types).extracting("type").containsExactlyInAnyOrder("A", "B")
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_TYPES_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )
    }

    @Test
    fun `should replace one set of organisation types with another`() {
      val organisation = testAPIClient.syncCreateAnOrganisation(syncCreateOrganisationRequest(4004L))

      val response = updateTypes(organisation.organisationId)

      with(response) {
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(types).hasSize(2)
        assertThat(types).extracting("type", "createdBy", "updatedBy").containsExactlyInAnyOrder(
          Tuple("A", "CREATOR", null),
          Tuple("B", "CREATOR", "UPDATER"),
        )
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_TYPES_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )

      resetEvents()

      val response2 = updateTypesVersionTwo(organisation.organisationId)

      with(response2) {
        assertThat(organisationId).isEqualTo(organisation.organisationId)
        assertThat(types).hasSize(3)
        assertThat(types).extracting("type", "createdBy", "updatedBy").containsExactlyInAnyOrder(
          Tuple("C", "CREATOR", null),
          Tuple("D", "CREATOR", "UPDATER"),
          Tuple("E", "CREATOR", "UPDATER"),
        )
      }

      stubEvents.assertHasEvent(
        event = OutboundEvent.ORGANISATION_TYPES_UPDATED,
        additionalInfo = OrganisationInfo(organisation.organisationId, organisation.organisationId, Source.NOMIS),
      )
    }

    private fun syncUpdateTypesRequest(organisationId: Long) = SyncUpdateTypesRequest(
      organisationId = organisationId,
      types = listOf(
        SyncOrganisationType(type = "A", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
        SyncOrganisationType(type = "B", createdBy = "CREATOR", createdTime = LocalDateTime.now(), updatedBy = "UPDATER"),
      ),
    )

    private fun syncUpdateTypesRequestVersionTwo(organisationId: Long) = SyncUpdateTypesRequest(
      organisationId = organisationId,
      types = listOf(
        SyncOrganisationType(type = "C", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
        SyncOrganisationType(type = "D", createdBy = "CREATOR", createdTime = LocalDateTime.now(), updatedBy = "UPDATER"),
        SyncOrganisationType(type = "E", createdBy = "CREATOR", createdTime = LocalDateTime.now(), updatedBy = "UPDATER"),
      ),
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

    private fun getTypesByOrganisationId(organisationId: Long) =
      webTestClient.get()
        .uri("/sync/organisation-types/{organisationId}", organisationId)
        .accept(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncTypesResponse::class.java)
        .returnResult().responseBody!!

    private fun updateTypes(organisationId: Long) =
      webTestClient.put()
        .uri("/sync/organisation-types/{organisationId}", organisationId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncUpdateTypesRequest(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncTypesResponse::class.java)
        .returnResult().responseBody!!

    private fun updateTypesVersionTwo(organisationId: Long) =
      webTestClient.put()
        .uri("/sync/organisation-types/{organisationId}", organisationId)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
        .bodyValue(syncUpdateTypesRequestVersionTwo(organisationId))
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody(SyncTypesResponse::class.java)
        .returnResult().responseBody!!
  }
}
