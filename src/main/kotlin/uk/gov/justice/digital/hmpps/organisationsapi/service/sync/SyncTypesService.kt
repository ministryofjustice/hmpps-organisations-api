package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeId
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateTypesRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncTypesResponse
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository

@Service
@Transactional
class SyncTypesService(
  val organisationRepository: OrganisationWithFixedIdRepository,
  val organisationTypeRepository: OrganisationTypeRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getTypesByOrganisationId(organisationId: Long): SyncTypesResponse {
    organisationRepository.findById(organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID $organisationId not found") }
    return organisationTypeRepository.getByIdOrganisationId(organisationId).toModel(organisationId)
  }

  fun updateTypes(organisationId: Long, request: SyncUpdateTypesRequest): SyncTypesResponse {
    organisationRepository.findById(organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID $organisationId not found") }

    val entitiesDeleted = organisationTypeRepository.deleteAllByOrganisationId(organisationId)
    logger.info("Organisation types for ID $organisationId:  Removed $entitiesDeleted, Create ${request.types.size}")

    return if (request.types.isEmpty()) {
      SyncTypesResponse(organisationId = organisationId, types = emptyList())
    } else {
      organisationTypeRepository.saveAll(
        request.types.map { each ->
          OrganisationTypeEntity(
            OrganisationTypeId(
              organisationId = organisationId,
              organisationType = each.type,
            ),
            createdBy = each.createdBy,
            createdTime = each.createdTime,
            updatedBy = each.updatedBy,
            updatedTime = each.updatedTime,
          )
        },
      ).toModel(organisationId)
    }
  }
}
