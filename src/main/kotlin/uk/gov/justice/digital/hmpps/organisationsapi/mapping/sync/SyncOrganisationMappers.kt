package uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync

import org.springframework.data.domain.Page
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationId
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse

fun OrganisationWithFixedIdEntity.toModel(): SyncOrganisationResponse = SyncOrganisationResponse(
  organisationId = this.id(),
  organisationName = this.organisationName,
  programmeNumber = this.programmeNumber,
  vatNumber = this.vatNumber,
  caseloadId = this.caseloadId,
  comments = this.comments,
  active = this.active,
  deactivatedDate = this.deactivatedDate,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationWithFixedIdEntity>.toModel(): List<SyncOrganisationResponse> = map { it.toModel() }

fun SyncCreateOrganisationRequest.toEntity() = OrganisationWithFixedIdEntity(
  organisationId = this.organisationId,
  organisationName = this.organisationName,
  programmeNumber = this.programmeNumber,
  vatNumber = this.vatNumber,
  caseloadId = this.caseloadId,
  comments = this.comments,
  active = this.active,
  deactivatedDate = this.deactivatedDate,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun OrganisationWithFixedIdEntity.toModelIds(): SyncOrganisationId = SyncOrganisationId(organisationId = this.organisationId)

fun Page<OrganisationWithFixedIdEntity>.toModelIds(): Page<SyncOrganisationId> = map { it.toModelIds() }
