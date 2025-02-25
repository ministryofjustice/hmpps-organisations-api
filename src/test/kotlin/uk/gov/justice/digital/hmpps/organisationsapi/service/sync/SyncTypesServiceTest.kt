package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeId
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncOrganisationType
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateTypesRequest
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import java.time.LocalDateTime
import java.util.Optional

class SyncTypesServiceTest {
  private val organisationRepository: OrganisationWithFixedIdRepository = mock()
  private val organisationTypeRepository: OrganisationTypeRepository = mock()

  private val syncService = SyncTypesService(
    organisationRepository,
    organisationTypeRepository,
  )

  @Test
  fun `should get an organisation's types by organisation ID`() {
    whenever(organisationRepository.findById(1L)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationTypeRepository.getByIdOrganisationId(1L)).thenReturn(
      listOfTypeEntities(1L),
    )

    val response = syncService.getTypesByOrganisationId(1L)

    with(response) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(types.size).isEqualTo(2)
      assertThat(types).extracting("createdBy").containsOnly("CREATOR")
      assertThat(types).extracting("type").containsExactlyInAnyOrder("A", "B")
    }

    verify(organisationTypeRepository).getByIdOrganisationId(1L)
  }

  @Test
  fun `should throw EntityNotFoundException if the organisation ID is not found`() {
    whenever(organisationRepository.findById(2L)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.getTypesByOrganisationId(2L)
    }

    verify(organisationRepository).findById(2L)
    verifyNoInteractions(organisationTypeRepository)
  }

  @Test
  fun `should update an organisation's types`() {
    val request = syncUpdateTypesRequest(3L)

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationTypeRepository.deleteAllByOrganisationId(3L)).thenReturn(2)
    whenever(organisationTypeRepository.saveAll(anyList())).thenReturn(listOfTypeEntities(3L))

    val response = syncService.updateTypes(3L, request)

    val captor = argumentCaptor<List<OrganisationTypeEntity>>()
    verify(organisationTypeRepository).saveAll(captor.capture())

    with(captor.firstValue) {
      assertThat(this.size).isEqualTo(2)
      assertThat(this[0].id.organisationId).isEqualTo(request.organisationId)
      assertThat(this[0].id.organisationType).isEqualTo("A")
      assertThat(this[1].id.organisationId).isEqualTo(request.organisationId)
      assertThat(this[1].id.organisationType).isEqualTo("B")
    }

    with(response) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(types.size).isEqualTo(2)
      assertThat(types).extracting("type").containsOnly("A", "B")
    }

    verify(organisationTypeRepository).deleteAllByOrganisationId(3L)
  }

  @Test
  fun `should succeed when the request contains an empty list of types`() {
    val request = syncUpdateTypesRequestEmpty(4L)

    whenever(organisationRepository.findById(4L)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationTypeRepository.deleteAllByOrganisationId(4L)).thenReturn(2)

    val response = syncService.updateTypes(4L, request)

    assertThat(response.organisationId).isEqualTo(4L)
    assertThat(response.types).isEmpty()

    verify(organisationTypeRepository, times(0)).saveAll(anyList())
  }

  private fun syncUpdateTypesRequest(organisationId: Long) = SyncUpdateTypesRequest(
    organisationId = organisationId,
    types = listOf(
      SyncOrganisationType(type = "A", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
      SyncOrganisationType(type = "B", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
    ),
  )

  private fun syncUpdateTypesRequestEmpty(organisationId: Long) = SyncUpdateTypesRequest(
    organisationId = organisationId,
    types = emptyList(),
  )

  private fun listOfTypeEntities(organisationId: Long) = listOf(
    OrganisationTypeEntity(
      id = OrganisationTypeId(organisationId, "A"),
      createdBy = "CREATOR",
      createdTime = LocalDateTime.now().minusHours(1),
      updatedBy = null,
      updatedTime = null,
    ),
    OrganisationTypeEntity(
      id = OrganisationTypeId(organisationId, "B"),
      createdBy = "CREATOR",
      createdTime = LocalDateTime.now().minusDays(2),
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now().minusDays(1),
    ),
  )

  private fun organisationEntity() = OrganisationWithFixedIdEntity(
    organisationId = 1L,
    organisationName = "Some Organisation",
    programmeNumber = "1234",
    vatNumber = "GB11111111",
    comments = "comment",
    caseloadId = null,
    active = true,
    deactivatedDate = null,
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
    updatedBy = null,
    updatedTime = null,
  )
}
