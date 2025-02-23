package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncEmailResponse
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationEmailRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository

@Service
@Transactional
class SyncEmailService(
  val organisationRepository: OrganisationWithFixedIdRepository,
  val organisationEmailRepository: OrganisationEmailRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getEmailById(organisationEmailId: Long): SyncEmailResponse {
    val emailEntity = organisationEmailRepository.findById(organisationEmailId)
      .orElseThrow { EntityNotFoundException("Organisation email address with ID $organisationEmailId not found") }
    return emailEntity.toModel()
  }

  fun createEmail(request: SyncCreateEmailRequest): SyncEmailResponse {
    organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }
    return organisationEmailRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateEmail(organisationEmailId: Long, request: SyncUpdateEmailRequest): SyncEmailResponse {
    val organisationEntity = organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }

    val emailEntity = organisationEmailRepository.findById(organisationEmailId)
      .orElseThrow { EntityNotFoundException("Organisation email address with ID $organisationEmailId not found") }

    if (organisationEntity.organisationId != emailEntity.organisationId) {
      logger.error("Organisation email update specified for an organisation which is not linked to this email address")
      throw ValidationException("Organisation ID ${organisationEntity.organisationId} is not linked to the email address ${emailEntity.organisationEmailId}")
    }

    val changedEmail = emailEntity.copy(
      organisationId = request.organisationId,
      emailAddress = request.emailAddress,
      updatedBy = request.updatedBy,
      updatedTime = request.updatedTime,
    )

    return organisationEmailRepository.saveAndFlush(changedEmail).toModel()
  }

  fun deleteEmail(organisationEmailId: Long): SyncEmailResponse {
    val emailEntity = organisationEmailRepository.findById(organisationEmailId)
      .orElseThrow { EntityNotFoundException("Organisation email with ID $organisationEmailId not found") }
    organisationEmailRepository.deleteById(organisationEmailId)
    return emailEntity.toModel()
  }
}
