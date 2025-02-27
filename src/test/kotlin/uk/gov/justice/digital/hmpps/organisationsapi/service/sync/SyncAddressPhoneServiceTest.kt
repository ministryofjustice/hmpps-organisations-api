package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationRepository
import java.time.LocalDateTime
import java.util.Optional

class SyncAddressPhoneServiceTest {
  private val organisationRepository: OrganisationRepository = mock()
  private val organisationAddressRepository: OrganisationAddressRepository = mock()
  private val organisationPhoneRepository: OrganisationPhoneRepository = mock()
  private val organisationAddressPhoneRepository: OrganisationAddressPhoneRepository = mock()

  private val syncService = SyncAddressPhoneService(
    organisationRepository,
    organisationAddressRepository,
    organisationPhoneRepository,
    organisationAddressPhoneRepository,
  )

  @Nested
  inner class SyncOrganisationAddressPhoneTests {
    @Test
    fun `should get an address-specific phone number by ID`() {
      whenever(organisationAddressPhoneRepository.findById(4L)).thenReturn(Optional.of(organisationAddressPhoneEntity()))
      whenever(organisationPhoneRepository.findById(2L)).thenReturn(Optional.of(organisationPhoneEntity()))

      val organisationAddressPhone = syncService.getAddressPhoneById(4L)

      with(organisationAddressPhone) {
        assertThat(organisationAddressPhoneId).isEqualTo(4L)
        assertThat(organisationAddressId).isEqualTo(3L)
        assertThat(organisationPhoneId).isEqualTo(2L)
        assertThat(organisationId).isEqualTo(1L)
        assertThat(phoneType).isEqualTo("MOB")
        assertThat(phoneNumber).isEqualTo("0909 111222")
        assertThat(extNumber).isNullOrEmpty()
        assertThat(createdBy).isEqualTo("CREATOR")
      }

      verify(organisationAddressPhoneRepository).findById(4L)
      verify(organisationPhoneRepository).findById(2L)
    }

    @Test
    fun `should fail to get an address-specific phone number when the ID is not found`() {
      whenever(organisationAddressPhoneRepository.findById(4L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.getAddressPhoneById(4L)
      }
      verify(organisationAddressPhoneRepository).findById(4L)
      verify(organisationPhoneRepository, never()).findById(any())
    }

    @Test
    fun `should fail to get an address-specific phone number when phone details are not found`() {
      whenever(organisationAddressPhoneRepository.findById(4L)).thenReturn(Optional.of(organisationAddressPhoneEntity()))
      whenever(organisationPhoneRepository.findById(2L)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        syncService.getAddressPhoneById(4L)
      }

      verify(organisationAddressPhoneRepository).findById(4L)
      verify(organisationPhoneRepository).findById(2L)
    }

    @Test
    fun `should create an address-specific phone number`() {
      val request = createOrganisationAddressPhoneRequest()

      whenever(organisationAddressRepository.findById(3L)).thenReturn(Optional.of(organisationAddressEntity()))
      whenever(organisationPhoneRepository.saveAndFlush(any())).thenReturn(organisationPhoneEntity())
      whenever(organisationAddressPhoneRepository.saveAndFlush(any())).thenReturn(organisationAddressPhoneEntity())

      val response = syncService.createAddressPhone(request)

      val phoneCaptor = argumentCaptor<OrganisationPhoneEntity>()

      verify(organisationPhoneRepository).saveAndFlush(phoneCaptor.capture())

      with(phoneCaptor.firstValue) {
        assertThat(phoneType).isEqualTo(request.phoneType)
        assertThat(phoneNumber).isEqualTo(request.phoneNumber)
        assertThat(extNumber).isEqualTo(request.extNumber)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }

      with(response) {
        assertThat(organisationAddressPhoneId).isEqualTo(4L)
        assertThat(organisationAddressId).isEqualTo(3L)
        assertThat(organisationPhoneId).isEqualTo(2L)
        assertThat(organisationId).isEqualTo(1L)
        assertThat(phoneType).isEqualTo(request.phoneType)
        assertThat(phoneNumber).isEqualTo(request.phoneNumber)
        assertThat(extNumber).isEqualTo(request.extNumber)
        assertThat(createdBy).isEqualTo(request.createdBy)
      }

      verify(organisationAddressRepository).findById(3L)
      verify(organisationPhoneRepository).saveAndFlush(any())
      verify(organisationAddressPhoneRepository).saveAndFlush(any())
    }

    @Test
    fun `should fail to create an address-specific phone number when the address is not found`() {
      val request = createOrganisationAddressPhoneRequest()
      whenever(organisationAddressRepository.findById(3L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.createAddressPhone(request)
      }
      verifyNoInteractions(organisationPhoneRepository)
      verifyNoInteractions(organisationAddressPhoneRepository)
    }

    @Test
    fun `should delete an address-specific phone number`() {
      whenever(organisationAddressPhoneRepository.findById(4L)).thenReturn(Optional.of(organisationAddressPhoneEntity()))
      whenever(organisationPhoneRepository.findById(2L)).thenReturn(Optional.of(organisationPhoneEntity()))

      syncService.deleteAddressPhone(4L)

      verify(organisationAddressPhoneRepository).findById(4L)
      verify(organisationPhoneRepository).findById(2L)
      verify(organisationPhoneRepository).deleteById(2L)
      verify(organisationAddressPhoneRepository).deleteById(4L)
    }

    @Test
    fun `should fail to delete an address-specific phone number if the ID is not found`() {
      whenever(organisationAddressPhoneRepository.findById(any())).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.deleteAddressPhone(4L)
      }
      verify(organisationAddressPhoneRepository).findById(any())
    }

    @Test
    fun `should update an address-specific phone number by ID`() {
      val request = updateOrganisationAddressPhoneRequest()

      whenever(organisationAddressPhoneRepository.findById(4L)).thenReturn(Optional.of(organisationAddressPhoneEntity()))
      whenever(organisationPhoneRepository.findById(2L)).thenReturn(Optional.of(organisationPhoneEntity()))
      whenever(organisationPhoneRepository.saveAndFlush(any())).thenReturn(organisationPhoneEntity())
      whenever(organisationAddressPhoneRepository.saveAndFlush(any())).thenReturn(
        organisationAddressPhoneEntity(updatedBy = request.updatedBy, updatedTime = request.updatedTime),
      )

      val updated = syncService.updateAddressPhone(4L, request)

      val phoneCaptor = argumentCaptor<OrganisationPhoneEntity>()

      verify(organisationPhoneRepository).saveAndFlush(phoneCaptor.capture())

      with(phoneCaptor.firstValue) {
        assertThat(phoneType).isEqualTo(request.phoneType)
        assertThat(phoneNumber).isEqualTo(request.phoneNumber)
        assertThat(extNumber).isEqualTo(request.extNumber)
        assertThat(updatedBy).isEqualTo(request.updatedBy)
        assertThat(updatedTime).isEqualTo(request.updatedTime)
      }

      with(updated) {
        assertThat(organisationAddressPhoneId).isEqualTo(4L)
        assertThat(organisationAddressId).isEqualTo(3L)
        assertThat(organisationPhoneId).isEqualTo(2L)
        assertThat(organisationId).isEqualTo(1L)
        assertThat(phoneType).isEqualTo(request.phoneType)
        assertThat(phoneNumber).isEqualTo(request.phoneNumber)
        assertThat(extNumber).isEqualTo(request.extNumber)
        assertThat(updatedBy).isEqualTo(request.updatedBy)
        assertThat(updatedTime).isEqualTo(request.updatedTime)
      }

      verify(organisationAddressPhoneRepository).findById(4L)
      verify(organisationPhoneRepository).findById(2L)
      verify(organisationPhoneRepository).saveAndFlush(any())
      verify(organisationAddressPhoneRepository).saveAndFlush(any())
    }

    @Test
    fun `should fail to update an address-specific phone number if the ID is not found`() {
      val request = updateOrganisationAddressPhoneRequest()
      whenever(organisationAddressPhoneRepository.findById(4L)).thenReturn(Optional.empty())
      assertThrows<EntityNotFoundException> {
        syncService.updateAddressPhone(4L, request)
      }
      verifyNoInteractions(organisationPhoneRepository)
    }

    @Test
    fun `should fail to update an address-specific phone number if the phone details are not found`() {
      val request = updateOrganisationAddressPhoneRequest()

      whenever(organisationAddressPhoneRepository.findById(4L)).thenReturn(Optional.of(organisationAddressPhoneEntity()))
      whenever(organisationPhoneRepository.findById(2L)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> {
        syncService.updateAddressPhone(4L, request)
      }

      verify(organisationAddressPhoneRepository).findById(4L)
      verify(organisationPhoneRepository).findById(2L)
      verify(organisationPhoneRepository, never()).saveAndFlush(any())
    }
  }

  private fun createOrganisationAddressPhoneRequest() = SyncCreateAddressPhoneRequest(
    organisationAddressId = 3L,
    phoneType = "MOB",
    phoneNumber = "0909 111222",
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
  )

  private fun updateOrganisationAddressPhoneRequest() = SyncUpdateAddressPhoneRequest(
    phoneType = "MOB",
    phoneNumber = "0909 111222",
    updatedBy = "UPDATER",
    updatedTime = LocalDateTime.now(),
  )

  private fun organisationAddressPhoneEntity(
    updatedBy: String? = null,
    updatedTime: LocalDateTime? = null,
  ) = OrganisationAddressPhoneEntity(
    organisationAddressPhoneId = 4L,
    organisationAddressId = 3L,
    organisationPhoneId = 2L,
    organisationId = 1L,
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
    updatedBy = updatedBy,
    updatedTime = updatedTime,
  )

  private fun organisationAddressEntity() = OrganisationAddressEntity(
    organisationAddressId = 3L,
    organisationId = 1L,
    createdBy = "CREATOR",
    primaryAddress = true,
    mailAddress = true,
    noFixedAddress = false,
    serviceAddress = false,
    createdTime = LocalDateTime.now(),
  )

  private fun organisationPhoneEntity() = OrganisationPhoneEntity(
    organisationPhoneId = 2L,
    organisationId = 1L,
    phoneType = "MOB",
    phoneNumber = "0909 111222",
    extNumber = null,
    createdBy = "CREATOR",
    createdTime = LocalDateTime.now(),
  )
}
