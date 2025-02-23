package uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWebAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncWebResponse

fun OrganisationWebAddressEntity.toModel() = SyncWebResponse(
  organisationWebAddressId = this.organisationWebAddressId,
  organisationId = this.organisationId,
  webAddress = this.webAddress,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationWebAddressEntity>.toModel(): List<SyncWebResponse> = map { it.toModel() }

fun SyncCreateWebRequest.toEntity() = OrganisationWebAddressEntity(
  organisationWebAddressId = 0L,
  organisationId = this.organisationId,
  webAddress = this.webAddress,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
)
