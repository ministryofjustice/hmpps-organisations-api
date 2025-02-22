package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import java.time.LocalDateTime
import java.util.Optional

class SyncPhoneServiceTest {
  private val organisationRepository: OrganisationWithFixedIdRepository = mock()
  private val organisationPhoneRepository: OrganisationPhoneRepository = mock()

  private val syncService = SyncPhoneService(
    organisationRepository,
    organisationPhoneRepository,
  )

  @Test
  fun `should get an phone number by ID`() {
    val organisationPhoneId = 1L

    val phoneEntity = phoneEntity(organisationPhoneId)

    whenever(organisationPhoneRepository.findById(organisationPhoneId)).thenReturn(Optional.of(phoneEntity))

    val phone = syncService.getPhoneById(organisationPhoneId)

    with(phone) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationPhoneId).isEqualTo(organisationPhoneId)
      assertThat(phoneType).isEqualTo(phoneEntity.phoneType)
      assertThat(phoneNumber).isEqualTo(phoneEntity.phoneNumber)
      assertThat(extNumber).isNull()
      assertThat(createdBy).isEqualTo("CREATOR")
    }

    verify(organisationPhoneRepository).findById(organisationPhoneId)
  }

  @Test
  fun `should throw EntityNotFoundException if the phone ID is not found`() {
    val organisationPhoneId = 2L

    whenever(organisationPhoneRepository.findById(organisationPhoneId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.getPhoneById(organisationPhoneId)
    }

    verify(organisationPhoneRepository).findById(organisationPhoneId)
  }

  @Test
  fun `should create a phone number`() {
    val organisationPhoneId = 3L

    val request = syncCreatePhoneRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationPhoneRepository.saveAndFlush(request.toEntity())).thenReturn(request.toEntity())

    val phone = syncService.createPhone(request)

    val captor = argumentCaptor<OrganisationPhoneEntity>()
    verify(organisationPhoneRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationPhoneId).isEqualTo(0L)
      assertThat(phoneType).isEqualTo(request.phoneType)
      assertThat(phoneNumber).isEqualTo(request.phoneNumber)
      assertThat(extNumber).isEqualTo(request.extNumber)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }

    with(phone) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationPhoneId).isEqualTo(0L)
      assertThat(phoneType).isEqualTo(request.phoneType)
      assertThat(phoneNumber).isEqualTo(request.phoneNumber)
      assertThat(extNumber).isEqualTo(request.extNumber)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }
  }

  @Test
  fun `should delete a phone number by ID`() {
    val organisationPhoneId = 4L

    whenever(organisationPhoneRepository.findById(organisationPhoneId)).thenReturn(
      Optional.of(phoneEntity(organisationPhoneId)),
    )

    val deleted = syncService.deletePhone(organisationPhoneId)

    with(deleted) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationPhoneId).isEqualTo(organisationPhoneId)
    }

    verify(organisationPhoneRepository).deleteById(organisationPhoneId)
  }

  @Test
  fun `should fail to delete a phone number when it was not found`() {
    val organisationPhoneId = 5L

    whenever(organisationPhoneRepository.findById(organisationPhoneId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.deletePhone(organisationPhoneId)
    }

    verify(organisationPhoneRepository).findById(organisationPhoneId)
  }

  @Test
  fun `should update an phone number`() {
    val organisationPhoneId = 6L

    val request = syncUpdatePhoneRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))

    whenever(organisationPhoneRepository.findById(organisationPhoneId)).thenReturn(
      Optional.of(phoneEntity(organisationPhoneId)),
    )

    whenever(organisationPhoneRepository.saveAndFlush(any())).thenReturn(
      request.toEntity(organisationPhoneId, "CREATOR", LocalDateTime.now().minusHours(1)),
    )

    val updated = syncService.updatePhone(organisationPhoneId, request)

    val captor = argumentCaptor<OrganisationPhoneEntity>()
    verify(organisationPhoneRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationPhoneId).isEqualTo(organisationPhoneId)
      assertThat(phoneType).isEqualTo(request.phoneType)
      assertThat(phoneNumber).isEqualTo(request.phoneNumber)
      assertThat(extNumber).isEqualTo(request.extNumber)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }

    with(updated) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationPhoneId).isEqualTo(organisationPhoneId)
      assertThat(phoneType).isEqualTo(request.phoneType)
      assertThat(phoneNumber).isEqualTo(request.phoneNumber)
      assertThat(extNumber).isEqualTo(request.extNumber)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }
  }

  @Test
  fun `should fail to update a phone number when not found`() {
    val organisationPhoneId = 7L

    val updateRequest = syncUpdatePhoneRequest()

    whenever(organisationRepository.findById(1L)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationPhoneRepository.findById(organisationPhoneId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.updatePhone(organisationPhoneId, updateRequest)
    }

    verify(organisationRepository).findById(1L)
    verify(organisationPhoneRepository).findById(organisationPhoneId)
  }

  private fun syncUpdatePhoneRequest() = SyncUpdatePhoneRequest(
    organisationId = 1L,
    phoneType = "MOB",
    phoneNumber = "07999 123456",
    extNumber = "3",
    updatedBy = "UPDATER",
    updatedTime = LocalDateTime.now(),
  )

  private fun syncCreatePhoneRequest() = SyncCreatePhoneRequest(
    organisationId = 1L,
    phoneType = "MOB",
    phoneNumber = "07999 123456",
    extNumber = null,
    createdTime = LocalDateTime.now(),
    createdBy = "CREATOR",
  )

  private fun phoneEntity(organisationPhoneId: Long) = OrganisationPhoneEntity(
    organisationPhoneId = organisationPhoneId,
    organisationId = 1L,
    phoneType = "MOB",
    phoneNumber = "07999 123456",
    extNumber = null,
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
  )

  private fun SyncUpdatePhoneRequest.toEntity(
    organisationPhoneId: Long,
    createdBy: String,
    createdTime: LocalDateTime,
  ) = OrganisationPhoneEntity(
    organisationId = this.organisationId,
    organisationPhoneId = organisationPhoneId,
    phoneType = this.phoneType,
    phoneNumber = this.phoneNumber,
    extNumber = this.extNumber,
    updatedBy = this.updatedBy,
    updatedTime = this.updatedTime,
    createdBy = createdBy,
    createdTime = createdTime,
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
