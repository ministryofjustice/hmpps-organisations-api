package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "v_organisation_summary")
data class OrganisationSummaryEntity(
  @Id
  val organisationId: Long,
  val organisationName: String,
  val organisationActive: Boolean,
  val flat: String?,
  val property: String?,
  val street: String?,
  val area: String?,
  val cityCode: String?,
  val cityDescription: String?,
  val countyCode: String?,
  val countyDescription: String?,
  val postCode: String?,
  val countryCode: String?,
  val countryDescription: String?,
  val businessPhoneNumber: String?,
  val businessPhoneNumberExtension: String?,
)
