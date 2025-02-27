package uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressPhoneResponse

fun OrganisationAddressPhoneEntity.toModel(phoneEntity: OrganisationPhoneEntity) = SyncAddressPhoneResponse(
  organisationAddressPhoneId = this.organisationAddressPhoneId,
  organisationAddressId = this.organisationAddressId,
  organisationPhoneId = this.organisationPhoneId,
  organisationId = this.organisationId,
  phoneType = phoneEntity.phoneType,
  phoneNumber = phoneEntity.phoneNumber,
  extNumber = phoneEntity.extNumber,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun SyncCreateAddressPhoneRequest.toEntity(phoneEntity: OrganisationPhoneEntity) = OrganisationAddressPhoneEntity(
  organisationAddressPhoneId = 0L,
  organisationAddressId = this.organisationAddressId,
  organisationPhoneId = phoneEntity.organisationPhoneId,
  organisationId = phoneEntity.organisationId,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
)
