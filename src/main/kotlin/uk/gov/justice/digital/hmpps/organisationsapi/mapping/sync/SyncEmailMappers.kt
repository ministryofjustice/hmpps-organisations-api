package uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEmailEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncEmailResponse

fun OrganisationEmailEntity.toModel() = SyncEmailResponse(
  organisationEmailId = this.organisationEmailId,
  organisationId = this.organisationId,
  emailAddress = this.emailAddress,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationEmailEntity>.toModel(): List<SyncEmailResponse> = map { it.toModel() }

fun SyncCreateEmailRequest.toEntity() = OrganisationEmailEntity(
  organisationEmailId = 0L,
  organisationId = this.organisationId,
  emailAddress = this.emailAddress,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
)
