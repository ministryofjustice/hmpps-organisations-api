package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import java.time.LocalDateTime
import java.util.Optional

class SyncOrganisationServiceTest {
  private val orgWithFixedIdRepository: OrganisationWithFixedIdRepository = mock()
  private val syncService = SyncOrganisationService(orgWithFixedIdRepository)

  @Nested
  inner class SyncOrganisationTests {
    @Test
    fun `should get an organisation by ID`() {
      whenever(orgWithFixedIdRepository.findById(1L)).thenReturn(Optional.of(orgWithFixedIdEntity(1L)))
      val organisation = syncService.getOrganisationById(1L)
      with(organisation) {
        assertThat(organisationId).isEqualTo(1L)
        assertThat(organisationName).isEqualTo("Some Organisation")
        assertThat(programmeNumber).isEqualTo("1234")
        assertThat(vatNumber).isEqualTo("GB11111111")
        assertThat(comments).isEqualTo("comment")
        assertThat(active).isTrue
        assertThat(createdBy).isEqualTo("CREATOR")
      }
      verify(orgWithFixedIdRepository).findById(1L)
    }

    @Test
    fun `should throw EntityNotFoundException if the ID is not found`() {
      whenever(orgWithFixedIdRepository.findById(1L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.getOrganisationById(1L)
      }
      verify(orgWithFixedIdRepository).findById(1L)
    }

    @Test
    fun `should create a contact`() {
      val request = syncCreateOrganisationRequest(2L)

      whenever(orgWithFixedIdRepository.existsById(2L)).thenReturn(false)
      whenever(orgWithFixedIdRepository.saveAndFlush(request.toEntity())).thenReturn(request.toEntity())

      val organisation = syncService.createOrganisation(request)

      val captor = argumentCaptor<OrganisationWithFixedIdEntity>()
      verify(orgWithFixedIdRepository).saveAndFlush(captor.capture())

      with(captor.firstValue) {
        assertThat(organisationId).isEqualTo(request.organisationId)
        assertThat(organisationName).isEqualTo(request.organisationName)
        assertThat(vatNumber).isEqualTo(request.vatNumber)
        assertThat(active).isEqualTo(request.active)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }

      with(organisation) {
        assertThat(organisationId).isEqualTo(request.organisationId)
        assertThat(organisationName).isEqualTo(request.organisationName)
        assertThat(vatNumber).isEqualTo(request.vatNumber)
        assertThat(active).isEqualTo(request.active)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }
    }

    @Test
    fun `should fail to create an organisation if the ID already exists`() {
      whenever(orgWithFixedIdRepository.existsById(3L)).thenReturn(true)
      val exceptionExpected = DuplicateOrganisationException("Sync: Duplicate organisation ID received 3")
      val request = syncCreateOrganisationRequest(3L)
      val exceptionThrown = assertThrows<DuplicateOrganisationException> {
        syncService.createOrganisation(request)
      }
      assertThat(exceptionThrown.message).isEqualTo(exceptionExpected.message)
      assertThat(exceptionThrown.javaClass).isEqualTo(exceptionExpected.javaClass)
      verify(orgWithFixedIdRepository, never()).saveAndFlush(any())
    }

    @Test
    fun `should delete an organisation by ID`() {
      whenever(orgWithFixedIdRepository.findById(4L)).thenReturn(Optional.of(orgWithFixedIdEntity(4L)))
      val deleted = syncService.deleteOrganisation(4L)
      with(deleted) {
        assertThat(organisationId).isEqualTo(4L)
        assertThat(organisationName).isEqualTo("Some Organisation")
      }
      verify(orgWithFixedIdRepository).deleteById(4L)
    }

    @Test
    fun `should fail to delete an organisation when it was not found`() {
      whenever(orgWithFixedIdRepository.findById(5L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.deleteOrganisation(5L)
      }
      verify(orgWithFixedIdRepository).findById(5L)
    }

    @Test
    fun `should update an organisation`() {
      val request = syncUpdateOrganisationRequest(6L)

      whenever(orgWithFixedIdRepository.findById(6L)).thenReturn(Optional.of(orgWithFixedIdEntity(6L)))
      whenever(orgWithFixedIdRepository.saveAndFlush(any())).thenReturn(
        request.toEntity(createdBy = "CREATOR", createdTime = LocalDateTime.now().minusHours(1)),
      )

      val updated = syncService.updateOrganisation(6L, request)

      val captor = argumentCaptor<OrganisationWithFixedIdEntity>()
      verify(orgWithFixedIdRepository).saveAndFlush(captor.capture())

      with(captor.firstValue) {
        assertThat(organisationId).isEqualTo(request.organisationId)
        assertThat(organisationName).isEqualTo(request.organisationName)
        assertThat(vatNumber).isEqualTo(request.vatNumber)
        assertThat(active).isEqualTo(request.active)
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(updatedBy).isEqualTo(request.updatedBy)
        assertThat(updatedTime).isEqualTo(request.updatedTime)
      }

      with(updated) {
        assertThat(organisationId).isEqualTo(request.organisationId)
        assertThat(organisationName).isEqualTo(request.organisationName)
        assertThat(vatNumber).isEqualTo(request.vatNumber)
        assertThat(active).isEqualTo(request.active)
        assertThat(createdBy).isEqualTo("CREATOR")
        assertThat(updatedBy).isEqualTo(request.updatedBy)
        assertThat(updatedTime).isEqualTo(request.updatedTime)
      }
    }

    @Test
    fun `should fail to update a contact when not found`() {
      val updateRequest = syncUpdateOrganisationRequest(7L)
      whenever(orgWithFixedIdRepository.findById(7L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updateOrganisation(7L, updateRequest)
      }
      verify(orgWithFixedIdRepository).findById(7L)
    }
  }

  private fun syncUpdateOrganisationRequest(organisationId: Long) = SyncUpdateOrganisationRequest(
    organisationId = organisationId,
    organisationName = "Some Organisation",
    programmeNumber = "1234",
    vatNumber = "GB11111111",
    comments = "comment",
    active = true,
    updatedBy = "UPDATER",
    updatedTime = LocalDateTime.now(),
  )

  private fun syncCreateOrganisationRequest(organisationId: Long) = SyncCreateOrganisationRequest(
    organisationId = organisationId,
    organisationName = "Some Organisation",
    active = true,
    createdTime = LocalDateTime.now(),
    createdBy = "CREATOR",
  )

  private fun orgWithFixedIdEntity(organisationId: Long) = OrganisationWithFixedIdEntity(
    organisationId = organisationId,
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

  fun SyncUpdateOrganisationRequest.toEntity(createdBy: String, createdTime: LocalDateTime) = OrganisationWithFixedIdEntity(
    organisationId = this.organisationId,
    organisationName = this.organisationName,
    programmeNumber = this.programmeNumber,
    vatNumber = this.vatNumber,
    caseloadId = this.caseloadId,
    comments = this.comments,
    active = this.active,
    deactivatedDate = this.deactivatedDate,
    updatedBy = this.updatedBy,
    updatedTime = this.updatedTime,
    createdBy = createdBy,
    createdTime = createdTime,
  )
}
