package uk.gov.justice.digital.hmpps.organisationsapi.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.organisationsapi.client.prisonregister.PrisonRegisterClient
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEmailEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeId
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWebAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2CreateRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationAddressDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationV2Details
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationEmailRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationSearchRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationSummaryRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWebAddressRepository
import java.time.LocalDate

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
  private val prisonRegisterClient: PrisonRegisterClient,
  private val organisationTypeRepository: OrganisationTypeRepository,
  private val organisationPhoneRepository: OrganisationPhoneRepository,
  private val organisationAddressEntityRepository: OrganisationAddressRepository,
  private val referenceCodeService: ReferenceCodeService,
) {

  fun getOrganisationById(id: Long): OrganisationDetails {
    val organisationEntity = organisationRepository.findById(id)
      .orElseThrow { EntityNotFoundException("Organisation with id $id not found") }

    // Split the address phones from the global phones
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
      caseloadPrisonName = organisationEntity.caseloadId?.let { caseloadId -> prisonRegisterClient.findPrisonNameById(caseloadId)?.prisonName },
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
    .map { address ->
      address.toModel(
        organisationAddressPhones.map { addressPhoneEntity ->
          addressPhoneEntity to organisationAddressPhoneNumbers.find {
            (it.organisationPhoneId == addressPhoneEntity.organisationPhoneId && addressPhoneEntity.organisationAddressId == address.organisationAddressId)
          }
        },
      )
    }

  fun search(request: OrganisationSearchRequest, pageable: Pageable): PagedModel<OrganisationSummary> = organisationSearchRepository.search(request, pageable).toModel()

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
      caseloadPrisonName = created.caseloadId?.let { caseLoadId -> prisonRegisterClient.findPrisonNameById(caseLoadId)?.prisonName },
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

  @Transactional
  fun createV2(request: OrganisationV2CreateRequest): OrganisationV2Details {
    validateV2ReferenceCodes(request)

    val organisation = organisationRepository.saveAndFlush(
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
    val organisationId = requireNotNull(organisation.organisationId)

    request.organisationTypes.forEach { type ->
      organisationTypeRepository.saveAndFlush(
        OrganisationTypeEntity(
          id = OrganisationTypeId(organisationId, type),
          createdBy = request.createdBy,
          createdTime = request.createdTime,
          updatedBy = request.updatedBy,
          updatedTime = request.updatedTime,
        ),
      )
    }

    request.phoneNumbers.forEach { phone ->
      saveV2Phone(organisationId, request, phone.phoneType, phone.phoneNumber, phone.extNumber)
    }

    request.internetAddresses.forEach { internetAddress ->
      when (internetAddress.type) {
        "EMAIL" -> organisationEmailRepository.saveAndFlush(
          OrganisationEmailEntity(
            organisationEmailId = 0,
            organisationId = organisationId,
            emailAddress = internetAddress.address,
            createdBy = request.createdBy,
            createdTime = request.createdTime,
            updatedBy = request.updatedBy,
            updatedTime = request.updatedTime,
          ),
        )
        "WEB" -> organisationWebAddressRepository.saveAndFlush(
          OrganisationWebAddressEntity(
            organisationWebAddressId = 0,
            organisationId = organisationId,
            webAddress = internetAddress.address,
            createdBy = request.createdBy,
            createdTime = request.createdTime,
            updatedBy = request.updatedBy,
            updatedTime = request.updatedTime,
          ),
        )
        else -> throw ValidationException("Unsupported internet address type: ${internetAddress.type}")
      }
    }

    request.addresses.forEach { addressRequest ->
      val address = organisationAddressEntityRepository.saveAndFlush(
        OrganisationAddressEntity(
          organisationAddressId = 0,
          organisationId = organisationId,
          addressType = addressRequest.addressType,
          primaryAddress = addressRequest.primaryAddress,
          mailAddress = addressRequest.mailAddress,
          serviceAddress = addressRequest.serviceAddress,
          noFixedAddress = addressRequest.noFixedAddress,
          flat = addressRequest.flat,
          property = addressRequest.property,
          street = addressRequest.street,
          area = addressRequest.area,
          cityCode = addressRequest.cityCode,
          countyCode = addressRequest.countyCode,
          postCode = addressRequest.postCode,
          countryCode = addressRequest.countryCode,
          specialNeedsCode = addressRequest.specialNeedsCode,
          contactPersonName = addressRequest.contactPersonName,
          businessHours = addressRequest.businessHours,
          comments = addressRequest.comments,
          startDate = addressRequest.startDate,
          endDate = addressRequest.endDate,
          active = addressRequest.endDate == null || !addressRequest.endDate.isBefore(LocalDate.now()),
          createdBy = request.createdBy,
          createdTime = request.createdTime,
          updatedBy = request.updatedBy,
          updatedTime = request.updatedTime,
        ),
      )

      addressRequest.phoneNumbers.forEach { phoneRequest ->
        val phone = saveV2Phone(
          organisationId,
          request,
          phoneRequest.phoneType,
          phoneRequest.phoneNumber,
          phoneRequest.extNumber,
        )
        organisationAddressPhoneRepository.saveAndFlush(
          OrganisationAddressPhoneEntity(
            organisationAddressPhoneId = 0,
            organisationId = organisationId,
            organisationPhoneId = phone.organisationPhoneId,
            organisationAddressId = address.organisationAddressId,
            createdBy = request.createdBy,
            createdTime = request.createdTime,
            updatedBy = request.updatedBy,
            updatedTime = request.updatedTime,
          ),
        )
      }
    }

    return OrganisationV2Details.from(organisationId, request)
  }

  private fun validateV2ReferenceCodes(request: OrganisationV2CreateRequest) {
    fun check(group: ReferenceCodeGroup, code: String?) {
      if (code != null) referenceCodeService.validateReferenceCode(group, code, allowInactive = false)
    }

    request.organisationTypes.forEach { check(ReferenceCodeGroup.ORGANISATION_TYPE, it) }
    request.phoneNumbers.forEach { check(ReferenceCodeGroup.PHONE_TYPE, it.phoneType) }
    request.addresses.forEach { address ->
      check(ReferenceCodeGroup.ADDRESS_TYPE, address.addressType)
      check(ReferenceCodeGroup.CITY, address.cityCode)
      check(ReferenceCodeGroup.COUNTY, address.countyCode)
      check(ReferenceCodeGroup.COUNTRY, address.countryCode)
      check(ReferenceCodeGroup.ORG_ADDRESS_SPECIAL_NEEDS, address.specialNeedsCode)
      address.phoneNumbers.forEach { check(ReferenceCodeGroup.PHONE_TYPE, it.phoneType) }
    }
  }

  private fun saveV2Phone(
    organisationId: Long,
    request: OrganisationV2CreateRequest,
    phoneType: String,
    phoneNumber: String,
    extNumber: String?,
  ) = organisationPhoneRepository.saveAndFlush(
    OrganisationPhoneEntity(
      organisationPhoneId = 0,
      organisationId = organisationId,
      phoneType = phoneType,
      phoneNumber = phoneNumber,
      extNumber = extNumber,
      createdBy = request.createdBy,
      createdTime = request.createdTime,
      updatedBy = request.updatedBy,
      updatedTime = request.updatedTime,
    ),
  )
}
