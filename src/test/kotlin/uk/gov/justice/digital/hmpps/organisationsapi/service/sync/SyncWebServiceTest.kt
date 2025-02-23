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
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWebAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWebAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import java.time.LocalDateTime
import java.util.Optional

class SyncWebServiceTest {
  private val organisationRepository: OrganisationWithFixedIdRepository = mock()
  private val organisationWebAddressRepository: OrganisationWebAddressRepository = mock()

  private val syncService = SyncWebService(
    organisationRepository,
    organisationWebAddressRepository,
  )

  @Test
  fun `should get an web address by ID`() {
    val organisationWebId = 1L

    val webEntity = webEntity(organisationWebId)

    whenever(organisationWebAddressRepository.findById(organisationWebId)).thenReturn(Optional.of(webEntity))

    val web = syncService.getWebAddressById(organisationWebId)

    with(web) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationWebAddressId).isEqualTo(organisationWebId)
      assertThat(webAddress).isEqualTo(webEntity.webAddress)
      assertThat(createdBy).isEqualTo("CREATOR")
    }

    verify(organisationWebAddressRepository).findById(organisationWebId)
  }

  @Test
  fun `should throw EntityNotFoundException if the web address ID is not found`() {
    val organisationWebId = 2L

    whenever(organisationWebAddressRepository.findById(organisationWebId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.getWebAddressById(organisationWebId)
    }

    verify(organisationWebAddressRepository).findById(organisationWebId)
  }

  @Test
  fun `should create an organisation web address`() {
    val request = syncCreateWebRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationWebAddressRepository.saveAndFlush(request.toEntity())).thenReturn(request.toEntity())

    val web = syncService.createWeb(request)

    val captor = argumentCaptor<OrganisationWebAddressEntity>()
    verify(organisationWebAddressRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationWebAddressId).isEqualTo(0L)
      assertThat(webAddress).isEqualTo(request.webAddress)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }

    with(web) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationWebAddressId).isEqualTo(0L)
      assertThat(webAddress).isEqualTo(request.webAddress)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }
  }

  @Test
  fun `should delete a web address by ID`() {
    val organisationWebId = 4L

    whenever(organisationWebAddressRepository.findById(organisationWebId)).thenReturn(
      Optional.of(webEntity(organisationWebId)),
    )

    val deleted = syncService.deleteWeb(organisationWebId)

    with(deleted) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationWebAddressId).isEqualTo(organisationWebId)
    }

    verify(organisationWebAddressRepository).deleteById(organisationWebId)
  }

  @Test
  fun `should fail to delete a web address when it was not found`() {
    val organisationWebId = 5L

    whenever(organisationWebAddressRepository.findById(organisationWebId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.deleteWeb(organisationWebId)
    }

    verify(organisationWebAddressRepository).findById(organisationWebId)
  }

  @Test
  fun `should update an web address`() {
    val organisationWebId = 6L

    val request = syncUpdateWebRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))

    whenever(organisationWebAddressRepository.findById(organisationWebId)).thenReturn(
      Optional.of(webEntity(organisationWebId)),
    )

    whenever(organisationWebAddressRepository.saveAndFlush(any())).thenReturn(
      request.toEntity(organisationWebId, "CREATOR", LocalDateTime.now().minusHours(1)),
    )

    val updated = syncService.updateWeb(organisationWebId, request)

    val captor = argumentCaptor<OrganisationWebAddressEntity>()
    verify(organisationWebAddressRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationWebAddressId).isEqualTo(organisationWebId)
      assertThat(webAddress).isEqualTo(request.webAddress)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }

    with(updated) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationWebAddressId).isEqualTo(organisationWebId)
      assertThat(webAddress).isEqualTo(request.webAddress)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }
  }

  @Test
  fun `should fail to update a web address when not found`() {
    val organisationWebId = 7L

    val updateRequest = syncUpdateWebRequest()

    whenever(organisationRepository.findById(1L)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationWebAddressRepository.findById(organisationWebId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.updateWeb(organisationWebId, updateRequest)
    }

    verify(organisationRepository).findById(1L)
    verify(organisationWebAddressRepository).findById(organisationWebId)
  }

  private fun syncUpdateWebRequest() = SyncUpdateWebRequest(
    organisationId = 1L,
    webAddress = "updated@exampe.com",
    updatedBy = "UPDATER",
    updatedTime = LocalDateTime.now(),
  )

  private fun syncCreateWebRequest() = SyncCreateWebRequest(
    organisationId = 1L,
    webAddress = "created@example.com",
    createdTime = LocalDateTime.now(),
    createdBy = "CREATOR",
  )

  private fun webEntity(organisationWebId: Long) = OrganisationWebAddressEntity(
    organisationWebAddressId = organisationWebId,
    organisationId = 1L,
    webAddress = "created@example.com",
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
  )

  private fun SyncUpdateWebRequest.toEntity(
    organisationWebId: Long,
    createdBy: String,
    createdTime: LocalDateTime,
  ) = OrganisationWebAddressEntity(
    organisationId = this.organisationId,
    organisationWebAddressId = organisationWebId,
    webAddress = this.webAddress,
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
