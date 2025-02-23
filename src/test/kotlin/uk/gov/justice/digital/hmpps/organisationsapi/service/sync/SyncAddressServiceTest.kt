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
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class SyncAddressServiceTest {
  private val organisationRepository: OrganisationWithFixedIdRepository = mock()
  private val organisationAddressRepository: OrganisationAddressRepository = mock()

  private val syncService = SyncAddressService(
    organisationRepository,
    organisationAddressRepository,
  )

  @Test
  fun `should get an organisation address by ID`() {
    val organisationAddressId = 1L

    val addressEntity = addressEntity(organisationAddressId)

    whenever(organisationAddressRepository.findById(organisationAddressId)).thenReturn(Optional.of(addressEntity))

    val address = syncService.getAddressById(organisationAddressId)

    with(address) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationAddressId).isEqualTo(organisationAddressId)
      assertThat(addressType).isEqualTo(addressEntity.addressType)
      assertThat(primaryAddress).isTrue()
      assertThat(mailAddress).isTrue()
      assertThat(serviceAddress).isFalse()
      assertThat(noFixedAddress).isFalse()
      assertThat(property).isEqualTo(addressEntity.property)
      assertThat(street).isEqualTo(addressEntity.street)
      assertThat(area).isEqualTo(addressEntity.area)
      assertThat(postcode).isEqualTo(addressEntity.postCode)
      assertThat(contactPersonName).isEqualTo(addressEntity.contactPersonName)
      assertThat(comments).isEqualTo(addressEntity.comments)
      assertThat(createdBy).isEqualTo(addressEntity.createdBy)
    }

    verify(organisationAddressRepository).findById(organisationAddressId)
  }

  @Test
  fun `should throw EntityNotFoundException if the address ID is not found`() {
    val organisationAddressId = 2L

    whenever(organisationAddressRepository.findById(organisationAddressId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.getAddressById(organisationAddressId)
    }

    verify(organisationAddressRepository).findById(organisationAddressId)
  }

  @Test
  fun `should create an organisation address`() {
    val request = syncCreateAddressRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationAddressRepository.saveAndFlush(request.toEntity())).thenReturn(request.toEntity())

    val address = syncService.createAddress(request)

    val captor = argumentCaptor<OrganisationAddressEntity>()
    verify(organisationAddressRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationAddressId).isEqualTo(0L)
      assertThat(addressType).isEqualTo(request.addressType)
      assertThat(primaryAddress).isEqualTo(request.primaryAddress)
      assertThat(mailAddress).isEqualTo(request.mailAddress)
      assertThat(serviceAddress).isEqualTo(request.serviceAddress)
      assertThat(noFixedAddress).isEqualTo(request.noFixedAddress)
      assertThat(property).isEqualTo(request.property)
      assertThat(street).isEqualTo(request.street)
      assertThat(area).isEqualTo(request.area)
      assertThat(postCode).isEqualTo(request.postcode)
      assertThat(contactPersonName).isEqualTo(request.contactPersonName)
      assertThat(comments).isEqualTo(request.comments)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }

    with(address) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationAddressId).isEqualTo(0L)
      assertThat(addressType).isEqualTo(request.addressType)
      assertThat(primaryAddress).isEqualTo(request.primaryAddress)
      assertThat(mailAddress).isEqualTo(request.mailAddress)
      assertThat(serviceAddress).isEqualTo(request.serviceAddress)
      assertThat(noFixedAddress).isEqualTo(request.noFixedAddress)
      assertThat(property).isEqualTo(request.property)
      assertThat(street).isEqualTo(request.street)
      assertThat(area).isEqualTo(request.area)
      assertThat(postcode).isEqualTo(request.postcode)
      assertThat(contactPersonName).isEqualTo(request.contactPersonName)
      assertThat(comments).isEqualTo(request.comments)
      assertThat(createdBy).isEqualTo(request.createdBy)
    }
  }

  @Test
  fun `should delete an address by ID`() {
    val organisationAddressId = 4L

    whenever(organisationAddressRepository.findById(organisationAddressId)).thenReturn(
      Optional.of(addressEntity(organisationAddressId)),
    )

    val deleted = syncService.deleteAddress(organisationAddressId)

    with(deleted) {
      assertThat(organisationId).isEqualTo(1L)
      assertThat(this.organisationAddressId).isEqualTo(organisationAddressId)
    }

    verify(organisationAddressRepository).deleteById(organisationAddressId)
  }

  @Test
  fun `should fail to delete an address when it was not found`() {
    val organisationAddressId = 5L

    whenever(organisationAddressRepository.findById(organisationAddressId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.deleteAddress(organisationAddressId)
    }

    verify(organisationAddressRepository).findById(organisationAddressId)
  }

  @Test
  fun `should update an address`() {
    val organisationAddressId = 6L

    val request = syncUpdateAddressRequest()

    whenever(organisationRepository.findById(request.organisationId)).thenReturn(Optional.of(organisationEntity()))

    whenever(organisationAddressRepository.findById(organisationAddressId)).thenReturn(
      Optional.of(addressEntity(organisationAddressId)),
    )

    whenever(organisationAddressRepository.saveAndFlush(any())).thenReturn(
      request.toEntity(organisationAddressId, "CREATOR", LocalDateTime.now().minusHours(1)),
    )

    val updated = syncService.updateAddress(organisationAddressId, request)

    val captor = argumentCaptor<OrganisationAddressEntity>()
    verify(organisationAddressRepository).saveAndFlush(captor.capture())

    with(captor.firstValue) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationAddressId).isEqualTo(organisationAddressId)
      assertThat(addressType).isEqualTo(request.addressType)
      assertThat(primaryAddress).isEqualTo(request.primaryAddress)
      assertThat(mailAddress).isEqualTo(request.mailAddress)
      assertThat(serviceAddress).isEqualTo(request.serviceAddress)
      assertThat(noFixedAddress).isEqualTo(request.noFixedAddress)
      assertThat(property).isEqualTo(request.property)
      assertThat(street).isEqualTo(request.street)
      assertThat(area).isEqualTo(request.area)
      assertThat(postCode).isEqualTo(request.postcode)
      assertThat(contactPersonName).isEqualTo(request.contactPersonName)
      assertThat(comments).isEqualTo(request.comments)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }

    with(updated) {
      assertThat(organisationId).isEqualTo(request.organisationId)
      assertThat(this.organisationAddressId).isEqualTo(organisationAddressId)
      assertThat(postcode).isEqualTo(request.postcode)
      assertThat(createdBy).isEqualTo("CREATOR")
      assertThat(updatedBy).isEqualTo(request.updatedBy)
      assertThat(updatedTime).isEqualTo(request.updatedTime)
    }
  }

  @Test
  fun `should fail to update a email address when not found`() {
    val organisationAddressId = 7L

    val updateRequest = syncUpdateAddressRequest()

    whenever(organisationRepository.findById(1L)).thenReturn(Optional.of(organisationEntity()))
    whenever(organisationAddressRepository.findById(organisationAddressId)).thenReturn(Optional.empty())

    assertThrows<EntityNotFoundException> {
      syncService.updateAddress(organisationAddressId, updateRequest)
    }

    verify(organisationRepository).findById(1L)
    verify(organisationAddressRepository).findById(organisationAddressId)
  }

  private fun syncUpdateAddressRequest() = SyncUpdateAddressRequest(
    organisationId = 1L,
    addressType = "BUS",
    primaryAddress = false,
    mailAddress = false,
    serviceAddress = false,
    noFixedAddress = false,
    property = "82",
    street = "The Street",
    area = "The Area",
    postcode = "A12 4AA",
    contactPersonName = "A contact name",
    comments = "updated comment",
    startDate = LocalDate.now().minusDays(10),
    updatedBy = "UPDATER",
    updatedTime = LocalDateTime.now(),
  )

  private fun syncCreateAddressRequest() = SyncCreateAddressRequest(
    organisationId = 1L,
    addressType = "HOME",
    primaryAddress = true,
    mailAddress = true,
    serviceAddress = false,
    noFixedAddress = false,
    property = "82",
    street = "The Street",
    area = "The Area",
    postcode = "A12 4AA",
    contactPersonName = "A contact name",
    comments = "comment",
    startDate = LocalDate.now().minusDays(10),
    createdTime = LocalDateTime.now().minusDays(10),
    createdBy = "CREATOR",
  )

  private fun addressEntity(organisationAddressId: Long) = OrganisationAddressEntity(
    organisationAddressId = organisationAddressId,
    organisationId = 1L,
    addressType = "HOME",
    primaryAddress = true,
    mailAddress = true,
    serviceAddress = false,
    noFixedAddress = false,
    property = "82",
    street = "The Street",
    area = "The Area",
    postCode = "A12 4AA",
    contactPersonName = "A contact name",
    comments = "comment",
    startDate = LocalDate.now().minusDays(10),
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
  )

  private fun SyncUpdateAddressRequest.toEntity(
    organisationAddressId: Long,
    createdBy: String,
    createdTime: LocalDateTime,
  ) = OrganisationAddressEntity(
    organisationId = this.organisationId,
    organisationAddressId = organisationAddressId,
    addressType = "BUS",
    primaryAddress = false,
    mailAddress = false,
    serviceAddress = false,
    noFixedAddress = false,
    property = "82",
    street = "The Street",
    area = "The Area",
    postCode = "A12 4AA",
    contactPersonName = "A contact name",
    comments = "updated comment",
    startDate = LocalDate.now().minusDays(10),
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
