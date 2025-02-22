package uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncPhoneResponse

fun OrganisationPhoneEntity.toModel(): SyncPhoneResponse = SyncPhoneResponse(
  organisationPhoneId = this.organisationPhoneId,
  organisationId = this.organisationId,
  phoneType = this.phoneType,
  phoneNumber = this.phoneNumber,
  extNumber = this.extNumber,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationPhoneEntity>.toModel(): List<SyncPhoneResponse> = map { it.toModel() }

fun SyncCreatePhoneRequest.toEntity() = OrganisationPhoneEntity(
  organisationPhoneId = 0L,
  organisationId = this.organisationId,
  phoneType = this.phoneType,
  phoneNumber = this.phoneNumber,
  extNumber = this.extNumber,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
)
