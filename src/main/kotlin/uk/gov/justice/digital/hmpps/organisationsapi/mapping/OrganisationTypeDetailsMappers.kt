package uk.gov.justice.digital.hmpps.organisationsapi.mapping

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeDetailsEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationTypeDetails

fun OrganisationTypeDetailsEntity.toModel(): OrganisationTypeDetails = OrganisationTypeDetails(
  organisationId = this.id.organisationId,
  organisationType = this.id.organisationType,
  organisationTypeDescription = this.organisationTypeDescription,
  createdBy = this.createdBy,
  createdTime = this.createdTime,
  updatedBy = this.updatedBy,
  updatedTime = this.updatedTime,
)

fun List<OrganisationTypeDetailsEntity>.toModel(): List<OrganisationTypeDetails> = map { it.toModel() }
