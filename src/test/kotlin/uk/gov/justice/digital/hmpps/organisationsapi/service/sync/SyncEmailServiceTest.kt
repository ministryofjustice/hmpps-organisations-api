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
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEmailEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationEmailRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import java.time.LocalDateTime
import java.util.Optional

class SyncEmailServiceTest {
  private val organisationRepository: OrganisationWithFixedIdRepository = mock()
  private val organisationEmailRepository: OrganisationEmailRepository = mock()

  private val syncService = SyncEmailService(
    organisationRepository,
    organisationEmailRepository,
  )

  @Test
  fun `should get an email address by ID`() {
    val organisationEmailId = 1L

    val emailEntity = emailEntity(organisationEmailId)

    whenever(organisationEmailRepository.findById(organisationEmailId)).thenReturn(Optional.of(emailEntity))

    val email = syncService.getEmailById(organisationEmailId)

    with(email) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationEmailId).isEqualTo(organisationEmailId)
      assertThat(emailAddress).isEqualTo(emailEntity.emailAddress)
      assertThat(createdBy).isEqualTo("CREATOR")
    }

    verify(organisationEmailRepository).findById(organisationEmailId)
  }

  @Test
  fun `should throw EntityNotFoundException if the email ID is not found`() {
    val organisationEmailId = 2L

    whenever(organisationEmailRepository.findById(organisationEmailId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.getEmailById(organisationEmailId)
    }

    verify(organisationEmailRepository).findById(organisationEmailId)
  }

  @Test
  fun `should create an organisation email address`() {
    val request = syncCreateEmailRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationEmailRepository.saveAndFlush(request.toEntity())).thenReturn(request.toEntity())

    val email = syncService.createEmail(request)

    val captor = argumentCaptor<OrganisationEmailEntity>()
    verify(organisationEmailRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationEmailId).isEqualTo(0L)
      assertThat(emailAddress).isEqualTo(request.emailAddress)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }

    with(email) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationEmailId).isEqualTo(0L)
      assertThat(emailAddress).isEqualTo(request.emailAddress)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }
  }

  @Test
  fun `should delete a email address by ID`() {
    val organisationEmailId = 4L

    whenever(organisationEmailRepository.findById(organisationEmailId)).thenReturn(
      Optional.of(emailEntity(organisationEmailId)),
    )

    val deleted = syncService.deleteEmail(organisationEmailId)

    with(deleted) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationEmailId).isEqualTo(organisationEmailId)
    }

    verify(organisationEmailRepository).deleteById(organisationEmailId)
  }

  @Test
  fun `should fail to delete an email address when it was not found`() {
    val organisationEmailId = 5L

    whenever(organisationEmailRepository.findById(organisationEmailId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.deleteEmail(organisationEmailId)
    }

    verify(organisationEmailRepository).findById(organisationEmailId)
  }

  @Test
  fun `should update an email address`() {
    val organisationEmailId = 6L

    val request = syncUpdateEmailRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))

    whenever(organisationEmailRepository.findById(organisationEmailId)).thenReturn(
      Optional.of(emailEntity(organisationEmailId)),
    )

    whenever(organisationEmailRepository.saveAndFlush(any())).thenReturn(
      request.toEntity(organisationEmailId, "CREATOR", LocalDateTime.now().minusHours(1)),
    )

    val updated = syncService.updateEmail(organisationEmailId, request)

    val captor = argumentCaptor<OrganisationEmailEntity>()
    verify(organisationEmailRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationEmailId).isEqualTo(organisationEmailId)
      assertThat(emailAddress).isEqualTo(request.emailAddress)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }

    with(updated) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationEmailId).isEqualTo(organisationEmailId)
      assertThat(emailAddress).isEqualTo(request.emailAddress)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }
  }

  @Test
  fun `should fail to update a email address when not found`() {
    val organisationEmailId = 7L

    val updateRequest = syncUpdateEmailRequest()

    whenever(organisationRepository.findById(1L)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationEmailRepository.findById(organisationEmailId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.updateEmail(organisationEmailId, updateRequest)
    }

    verify(organisationRepository).findById(1L)
    verify(organisationEmailRepository).findById(organisationEmailId)
  }

  private fun syncUpdateEmailRequest() = SyncUpdateEmailRequest(
    organisationId = 1L,
    emailAddress = "updated@exampe.com",
    updatedBy = "UPDATER",
    updatedTime = LocalDateTime.now(),
  )

  private fun syncCreateEmailRequest() = SyncCreateEmailRequest(
    organisationId = 1L,
    emailAddress = "created@example.com",
    createdTime = LocalDateTime.now(),
    createdBy = "CREATOR",
  )

  private fun emailEntity(organisationEmailId: Long) = OrganisationEmailEntity(
    organisationEmailId = organisationEmailId,
    organisationId = 1L,
    emailAddress = "created@example.com",
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
  )

  private fun SyncUpdateEmailRequest.toEntity(
    organisationEmailId: Long,
    createdBy: String,
    createdTime: LocalDateTime,
  ) = OrganisationEmailEntity(
    organisationId = this.organisationId,
    organisationEmailId = organisationEmailId,
    emailAddress = this.emailAddress,
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
