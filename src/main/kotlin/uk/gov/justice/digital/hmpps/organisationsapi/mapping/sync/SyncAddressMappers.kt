package uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressResponse

fun OrganisationAddressEntity.toModel() = SyncAddressResponse(
  organisationAddressId = this.organisationAddressId,
  organisationId = this.organisationId,
  addressType = this.addressType,
  primaryAddress = this.primaryAddress,
  mailAddress = this.mailAddress,
  serviceAddress = this.serviceAddress,
  noFixedAddress = this.noFixedAddress,
  flat = this.flat,
  property = this.property,
  street = this.street,
  area = this.area,
  cityCode = this.cityCode,
  countyCode = this.countyCode,
  postcode = this.postCode,
  countryCode = this.countryCode,
  specialNeedsCode = this.specialNeedsCode,
  contactPersonName = this.contactPersonName,
  businessHours = this.businessHours,
  comments = this.comments,
  startDate = this.startDate,
  endDate = this.endDate,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationAddressEntity>.toModel(): List<SyncAddressResponse> = map { it.toModel() }

fun SyncCreateAddressRequest.toEntity() = OrganisationAddressEntity(
  organisationAddressId = 0L,
  organisationId = this.organisationId,
  addressType = this.addressType,
  primaryAddress = this.primaryAddress,
  mailAddress = this.mailAddress,
  serviceAddress = this.serviceAddress,
  noFixedAddress = this.noFixedAddress,
  flat = this.flat,
  property = this.property,
  street = this.street,
  area = this.area,
  cityCode = this.cityCode,
  countyCode = this.countyCode,
  postCode = this.postcode,
  countryCode = this.countryCode,
  specialNeedsCode = this.specialNeedsCode,
  contactPersonName = this.contactPersonName,
  businessHours = this.businessHours,
  comments = this.comments,
  startDate = this.startDate,
  endDate = this.endDate,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
)
