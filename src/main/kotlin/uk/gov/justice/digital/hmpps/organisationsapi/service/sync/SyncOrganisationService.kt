package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModelIds
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationId
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository

@Service
@Transactional
class SyncOrganisationService(
  val organisationRepository: OrganisationWithFixedIdRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getOrganisationById(organisationId: Long): SyncOrganisationResponse {
    val orgWithFixedIdEntity = organisationRepository.findById(organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID $organisationId not found") }
    return orgWithFixedIdEntity.toModel()
  }

  /**
   * A delete via sync will attempt to remove the organisation only.
   * If there are still sub-elements like addresses, types, phones or emails
   * this operation will fail.
   */

  fun deleteOrganisation(organisationId: Long): SyncOrganisationResponse {
    val orgWithFixedIdEntity = organisationRepository.findById(organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID $organisationId not found") }
    organisationRepository.deleteById(organisationId)
    return orgWithFixedIdEntity.toModel()
  }

  /**
   * Creation of an organisation via sync will accept the NOMIS corporate_id and use this as
   * the primary key (organisation_id) in the organisation database. There are two different sequence
   * ranges for organisation_id - one for those created in NOMIS and another for those created
   * in DPS. The ranges cannot overlap.
   */
  fun createOrganisation(request: SyncCreateOrganisationRequest): SyncOrganisationResponse {
    if (organisationRepository.existsById(request.organisationId)) {
      val message = "Sync: Duplicate organisation ID received ${request.organisationId}"
      logger.error(message)
      throw DuplicateOrganisationException(message)
    }
    return organisationRepository.saveAndFlush(request.toEntity()).toModel()
  }

  /**
   * Updates via sync will receive the whole details again from NOMIS, so update
   * all columns with the values provided. This is not a PATCH type update.
   */

  fun updateOrganisation(organisationId: Long, request: SyncUpdateOrganisationRequest): SyncOrganisationResponse {
    val orgWithFixedIdEntity = organisationRepository.findById(organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID $organisationId not found") }

    val changedOrganisation = orgWithFixedIdEntity.copy(
      organisationName = request.organisationName,
      programmeNumber = request.programmeNumber,
      vatNumber = request.vatNumber,
      caseloadId = request.caseloadId,
      comments = request.comments,
      active = request.active,
      deactivatedDate = request.deactivatedDate,
      updatedBy = request.updatedBy,
      updatedTime = request.updatedTime,
    )

    return organisationRepository.saveAndFlush(changedOrganisation).toModel()
  }

  fun getOrganisationIds(pageable: Pageable): Page<SyncOrganisationId> = organisationRepository.findAll(pageable).toModelIds()
}
