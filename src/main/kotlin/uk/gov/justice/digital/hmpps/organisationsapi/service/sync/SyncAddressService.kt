package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressResponse
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository

@Service
@Transactional
class SyncAddressService(
  val organisationRepository: OrganisationWithFixedIdRepository,
  val organisationAddressRepository: OrganisationAddressRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getAddressById(organisationAddressId: Long): SyncAddressResponse {
    val addressEntity = organisationAddressRepository.findById(organisationAddressId)
      .orElseThrow { EntityNotFoundException("Organisation address with ID $organisationAddressId not found") }
    return addressEntity.toModel()
  }

  fun createAddress(request: SyncCreateAddressRequest): SyncAddressResponse {
    organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }
    return organisationAddressRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateAddress(organisationAddressId: Long, request: SyncUpdateAddressRequest): SyncAddressResponse {
    val organisationEntity = organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }

    val addressEntity = organisationAddressRepository.findById(organisationAddressId)
      .orElseThrow { EntityNotFoundException("Organisation address with ID $organisationAddressId not found") }

    if (organisationEntity.organisationId != addressEntity.organisationId) {
      logger.error("Organisation address update specified for an organisation which is not linked to this address")
      throw ValidationException("Organisation ID ${organisationEntity.organisationId} is not linked to the address ${addressEntity.organisationAddressId}")
    }

    val changedAddress = addressEntity.copy(
      organisationId = request.organisationId,
      addressType = request.addressType,
      primaryAddress = request.primaryAddress,
      mailAddress = request.mailAddress,
      serviceAddress = request.serviceAddress,
      noFixedAddress = request.noFixedAddress,
      flat = request.flat,
      property = request.property,
      street = request.street,
      area = request.area,
      cityCode = request.cityCode,
      countyCode = request.countyCode,
      postCode = request.postcode,
      countryCode = request.countryCode,
      specialNeedsCode = request.specialNeedsCode,
      contactPersonName = request.contactPersonName,
      businessHours = request.businessHours,
      comments = request.comments,
      startDate = request.startDate,
      endDate = request.endDate,
      updatedBy = request.updatedBy,
      updatedTime = request.updatedTime,
    )

    return organisationAddressRepository.saveAndFlush(changedAddress).toModel()
  }

  fun deleteAddress(organisationAddressId: Long): SyncAddressResponse {
    val addressEntity = organisationAddressRepository.findById(organisationAddressId)
      .orElseThrow { EntityNotFoundException("Organisation address with ID $organisationAddressId not found") }
    organisationAddressRepository.deleteById(organisationAddressId)
    return addressEntity.toModel()
  }
}
