package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationRepository

@Service
@Transactional
class SyncAddressPhoneService(
  val organisationRepository: OrganisationRepository,
  val organisationAddressRepository: OrganisationAddressRepository,
  val organisationPhoneRepository: OrganisationPhoneRepository,
  val organisationAddressPhoneRepository: OrganisationAddressPhoneRepository,
) {

  @Transactional(readOnly = true)
  fun getAddressPhoneById(organisationAddressPhoneId: Long): SyncAddressPhoneResponse {
    val organisationAddressPhoneEntity = organisationAddressPhoneRepository.findById(organisationAddressPhoneId)
      .orElseThrow { EntityNotFoundException("Address-specific phone number with ID $organisationAddressPhoneId not found") }

    val organisationPhoneEntity = organisationPhoneRepository.findById(organisationAddressPhoneEntity.organisationPhoneId)
      .orElseThrow { EntityNotFoundException("Phone number with ID ${organisationAddressPhoneEntity.organisationPhoneId} not found") }

    return organisationAddressPhoneEntity.toModel(organisationPhoneEntity)
  }

  fun createAddressPhone(request: SyncCreateAddressPhoneRequest): SyncAddressPhoneResponse {
    val organisationAddressEntity = organisationAddressRepository.findById(request.organisationAddressId)
      .orElseThrow { EntityNotFoundException("Organisation address with ID ${request.organisationAddressId} was not found") }

    val phoneEntity = organisationPhoneRepository.saveAndFlush(
      OrganisationPhoneEntity(
        organisationPhoneId = 0L,
        organisationId = organisationAddressEntity.organisationId,
        phoneType = request.phoneType,
        phoneNumber = request.phoneNumber,
        extNumber = request.extNumber,
        createdBy = request.createdBy,
        createdTime = request.createdTime,
      ),
    )

    return organisationAddressPhoneRepository.saveAndFlush(request.toEntity(phoneEntity)).toModel(phoneEntity)
  }

  fun updateAddressPhone(organisationAddressPhoneId: Long, request: SyncUpdateAddressPhoneRequest): SyncAddressPhoneResponse {
    val organisationAddressPhone = organisationAddressPhoneRepository.findById(organisationAddressPhoneId)
      .orElseThrow { EntityNotFoundException("Organisation address phone with ID $organisationAddressPhoneId not found") }

    val phoneEntity = organisationPhoneRepository.findById(organisationAddressPhone.organisationPhoneId)
      .orElseThrow { EntityNotFoundException("Organisation phone with ID ${organisationAddressPhone.organisationPhoneId} not found") }

    val updatedPhone = organisationPhoneRepository.saveAndFlush(
      phoneEntity.copy(
        phoneType = request.phoneType,
        phoneNumber = request.phoneNumber,
        extNumber = request.extNumber,
        updatedBy = request.updatedBy,
        updatedTime = request.updatedTime,
      ),
    )

    // Also update the updatedBy, updatedTime on the address phone row
    val updatedAddressPhone = organisationAddressPhoneRepository.saveAndFlush(
      organisationAddressPhone.copy(
        updatedBy = request.updatedBy,
        updatedTime = request.updatedTime,
      ),
    )

    return updatedAddressPhone.toModel(updatedPhone)
  }

  fun deleteAddressPhone(organisationAddressPhoneId: Long): SyncAddressPhoneResponse {
    val addressPhoneToDelete = organisationAddressPhoneRepository.findById(organisationAddressPhoneId)
      .orElseThrow { EntityNotFoundException("Address-specific phone number with ID $organisationAddressPhoneId was not found") }

    val phoneToDelete = organisationPhoneRepository.findById(addressPhoneToDelete.organisationPhoneId)
      .orElseThrow { EntityNotFoundException("Organisation phone with ID ${addressPhoneToDelete.organisationPhoneId} was not found") }

    organisationAddressPhoneRepository.deleteById(addressPhoneToDelete.organisationAddressPhoneId)
    organisationPhoneRepository.deleteById(phoneToDelete.organisationPhoneId)

    return addressPhoneToDelete.toModel(phoneToDelete)
  }
}
