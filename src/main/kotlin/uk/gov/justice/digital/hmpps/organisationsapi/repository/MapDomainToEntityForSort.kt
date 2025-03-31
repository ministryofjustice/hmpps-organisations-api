package uk.gov.justice.digital.hmpps.organisationsapi.repository

import jakarta.validation.ValidationException
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationSummaryEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary

fun mapSortPropertiesOfOrgSearch(property: String): String = when (property) {
  OrganisationSummary::organisationId.name -> OrganisationSummaryEntity::organisationId.name
  OrganisationSummary::organisationName.name -> OrganisationSummaryEntity::organisationName.name
  OrganisationSummary::organisationActive.name -> OrganisationSummaryEntity::organisationActive.name
  OrganisationSummary::flat.name -> OrganisationSummaryEntity::flat.name
  OrganisationSummary::property.name -> OrganisationSummaryEntity::property.name
  OrganisationSummary::street.name -> OrganisationSummaryEntity::street.name
  OrganisationSummary::area.name -> OrganisationSummaryEntity::area.name
  OrganisationSummary::cityCode.name -> OrganisationSummaryEntity::cityCode.name
  OrganisationSummary::cityDescription.name -> OrganisationSummaryEntity::cityDescription.name
  OrganisationSummary::countyCode.name -> OrganisationSummaryEntity::countyCode.name
  OrganisationSummary::countyDescription.name -> OrganisationSummaryEntity::countyDescription.name
  OrganisationSummary::postcode.name -> OrganisationSummaryEntity::postCode.name
  OrganisationSummary::countryCode.name -> OrganisationSummaryEntity::countryCode.name
  OrganisationSummary::countryDescription.name -> OrganisationSummaryEntity::countryDescription.name
  OrganisationSummary::businessPhoneNumber.name -> OrganisationSummaryEntity::businessPhoneNumber.name
  OrganisationSummary::businessPhoneNumberExtension.name -> OrganisationSummaryEntity::businessPhoneNumberExtension.name
  else -> throw ValidationException("Unable to sort on $property")
}
