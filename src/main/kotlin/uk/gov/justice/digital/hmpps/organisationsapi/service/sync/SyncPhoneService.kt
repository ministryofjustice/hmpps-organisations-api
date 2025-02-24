package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository

@Service
@Transactional
class SyncPhoneService(
  val organisationRepository: OrganisationWithFixedIdRepository,
  val organisationPhoneRepository: OrganisationPhoneRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getPhoneById(organisationPhoneId: Long): SyncPhoneResponse {
    val phoneEntity = organisationPhoneRepository.findById(organisationPhoneId)
      .orElseThrow { EntityNotFoundException("Organisation phone with ID $organisationPhoneId not found") }
    return phoneEntity.toModel()
  }

  fun createPhone(request: SyncCreatePhoneRequest): SyncPhoneResponse {
    organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }
    return organisationPhoneRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updatePhone(organisationPhoneId: Long, request: SyncUpdatePhoneRequest): SyncPhoneResponse {
    val organisationEntity = organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }

    val phoneEntity = organisationPhoneRepository.findById(organisationPhoneId)
      .orElseThrow { EntityNotFoundException("Organisation phone with ID $organisationPhoneId not found") }

    if (organisationEntity.organisationId != phoneEntity.organisationId) {
      logger.error("Organisation phone update specified for an organisation which is not linked to this phone")
      throw ValidationException("Organisation ID ${organisationEntity.organisationId} is not linked to the phone ${phoneEntity.organisationPhoneId}")
    }

    val changedPhone = phoneEntity.copy(
      organisationId = request.organisationId,
      phoneType = request.phoneType,
      phoneNumber = request.phoneNumber,
      extNumber = request.extNumber,
      updatedBy = request.updatedBy,
      updatedTime = request.updatedTime,
    )

    return organisationPhoneRepository.saveAndFlush(changedPhone).toModel()
  }

  fun deletePhone(organisationPhoneId: Long): SyncPhoneResponse {
    val phoneEntity = organisationPhoneRepository.findById(organisationPhoneId)
      .orElseThrow { EntityNotFoundException("Organisation phone with ID $organisationPhoneId not found") }
    organisationPhoneRepository.deleteById(organisationPhoneId)
    return phoneEntity.toModel()
  }
}
