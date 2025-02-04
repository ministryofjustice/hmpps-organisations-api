package uk.gov.justice.digital.hmpps.organisationsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationAddressDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationEmailRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationSearchRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationSummaryRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWebAddressRepository

@Service
class OrganisationService(
  private val organisationRepository: OrganisationRepository,
  private val organisationSearchRepository: OrganisationSearchRepository,
  private val organisationPhoneDetailsRepository: OrganisationPhoneDetailsRepository,
  private val organisationAddressPhoneRepository: OrganisationAddressPhoneRepository,
  private val organisationTypeDetailsRepository: OrganisationTypeDetailsRepository,
  private val organisationEmailRepository: OrganisationEmailRepository,
  private val organisationWebAddressRepository: OrganisationWebAddressRepository,
  private val organisationAddressRepository: OrganisationAddressDetailsRepository,
  private val organisationSummaryRepository: OrganisationSummaryRepository,
) {

  fun getOrganisationById(id: Long): OrganisationDetails {
    val organisationEntity = organisationRepository.findById(id)
      .orElseThrow { EntityNotFoundException("Organisation with id $id not found") }
    val phoneNumbers = organisationPhoneDetailsRepository.findByOrganisationId(id)
    val organisationAddressPhones = organisationAddressPhoneRepository.findByOrganisationId(id)
    val organisationAddressPhoneIds = organisationAddressPhones.map { it.organisationPhoneId }
    val (organisationAddressPhoneNumbers, organisationPhoneNumbers) = phoneNumbers.partition { it.organisationPhoneId in organisationAddressPhoneIds }
    return OrganisationDetails(
      organisationId = organisationEntity.organisationId!!,
      organisationName = organisationEntity.organisationName,
      programmeNumber = organisationEntity.programmeNumber,
      vatNumber = organisationEntity.vatNumber,
      caseloadId = organisationEntity.caseloadId,
      comments = organisationEntity.comments,
      active = organisationEntity.active,
      deactivatedDate = organisationEntity.deactivatedDate,
      organisationTypes = organisationTypeDetailsRepository.findByIdOrganisationId(id).toModel(),
      phoneNumbers = organisationPhoneNumbers.toModel(),
      emailAddresses = organisationEmailRepository.findByOrganisationId(id).toModel(),
      webAddresses = organisationWebAddressRepository.findByOrganisationId(id).toModel(),
      addresses = addressesWithPhoneNumbers(id, organisationAddressPhoneNumbers, organisationAddressPhones),
      createdBy = organisationEntity.createdBy,
      createdTime = organisationEntity.createdTime,
      updatedBy = organisationEntity.updatedBy,
      updatedTime = organisationEntity.updatedTime,
    )
  }

  fun getOrganisationSummaryById(id: Long): OrganisationSummary {
    val entity = organisationSummaryRepository.findById(id)
      .orElseThrow { EntityNotFoundException("Organisation with id $id not found") }
    return entity.toModel()
  }

  private fun addressesWithPhoneNumbers(
    id: Long,
    organisationAddressPhoneNumbers: List<OrganisationPhoneDetailsEntity>,
    organisationAddressPhones: List<OrganisationAddressPhoneEntity>,
  ): List<OrganisationAddressDetails> = organisationAddressRepository.findByOrganisationId(id)
    .map { address -> address.toModel(organisationAddressPhones.map { addressPhoneEntity -> addressPhoneEntity to organisationAddressPhoneNumbers.find { it.organisationPhoneId == addressPhoneEntity.organisationPhoneId }!! }) }

  fun search(request: OrganisationSearchRequest, pageable: Pageable): Page<OrganisationSummary> = organisationSearchRepository.search(request, pageable).toModel()

  @Transactional
  fun create(request: CreateOrganisationRequest): OrganisationDetails {
    val created = organisationRepository.saveAndFlush(
      OrganisationEntity(
        organisationName = request.organisationName,
        programmeNumber = request.programmeNumber,
        vatNumber = request.vatNumber,
        caseloadId = request.caseloadId,
        comments = request.comments,
        active = request.active,
        deactivatedDate = request.deactivatedDate,
        createdBy = request.createdBy,
        createdTime = request.createdTime,
        updatedBy = request.updatedBy,
        updatedTime = request.updatedTime,
      ),
    )
    return OrganisationDetails(
      organisationId = created.organisationId!!,
      organisationName = created.organisationName,
      programmeNumber = created.programmeNumber,
      vatNumber = created.vatNumber,
      caseloadId = created.caseloadId,
      comments = created.comments,
      active = created.active,
      deactivatedDate = created.deactivatedDate,
      organisationTypes = emptyList(),
      phoneNumbers = emptyList(),
      emailAddresses = emptyList(),
      webAddresses = emptyList(),
      addresses = emptyList(),
      createdBy = created.createdBy,
      createdTime = created.createdTime,
      updatedBy = created.updatedBy,
      updatedTime = created.updatedTime,
    )
  }
}
