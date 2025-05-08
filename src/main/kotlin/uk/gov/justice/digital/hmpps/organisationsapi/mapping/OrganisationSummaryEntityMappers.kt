package uk.gov.justice.digital.hmpps.organisationsapi.mapping

import org.springframework.data.domain.Page
import org.springframework.data.web.PagedModel
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationSummaryEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary

fun OrganisationSummaryEntity.toModel(): OrganisationSummary = OrganisationSummary(
  organisationId = this.organisationId,
  organisationName = this.organisationName,
  organisationActive = this.organisationActive,
  flat = this.flat,
  property = this.property,
  street = this.street,
  area = this.area,
  cityCode = this.cityCode,
  cityDescription = this.cityDescription,
  countyCode = this.countyCode,
  countyDescription = this.countyDescription,
  postcode = this.postCode,
  countryCode = this.countryCode,
  countryDescription = this.countryDescription,
  businessPhoneNumber = this.businessPhoneNumber,
  businessPhoneNumberExtension = this.businessPhoneNumberExtension,
)

fun Page<OrganisationSummaryEntity>.toModel(): PagedModel<OrganisationSummary> = PagedModel(map { it.toModel() })
