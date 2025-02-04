package uk.gov.justice.digital.hmpps.organisationsapi.mapping

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneDetailsEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationPhoneDetails

fun OrganisationPhoneDetailsEntity.toModel(): OrganisationPhoneDetails = OrganisationPhoneDetails(
  organisationPhoneId = this.organisationPhoneId,
  organisationId = this.organisationId,
  phoneType = this.phoneType,
  phoneTypeDescription = this.phoneTypeDescription,
  phoneNumber = this.phoneNumber,
  extNumber = this.extNumber,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationPhoneDetailsEntity>.toModel(): List<OrganisationPhoneDetails> = map { it.toModel() }
