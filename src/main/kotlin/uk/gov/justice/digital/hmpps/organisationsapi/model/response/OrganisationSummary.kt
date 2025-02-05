package uk.gov.justice.digital.hmpps.organisationsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The high level details of an organisation, it's primary address and any business phone number associated with that address.")
data class OrganisationSummary(
  @Schema(description = "The organisation id", example = "123456789")
  val organisationId: Long,

  @Schema(description = "The name of the organisation", example = "Bob's Bakery")
  val organisationName: String,

  @Schema(description = "Whether the organisation is currently active or not", example = "true")
  val organisationActive: Boolean,

  @Schema(description = "Flat number in the address, if any", example = "Flat 1", nullable = true)
  val flat: String?,

  @Schema(description = "Property name or number, if any", example = "123", nullable = true)
  val property: String?,

  @Schema(description = "Street name, if any", example = "Baker Street", nullable = true)
  val street: String?,

  @Schema(description = "Area or locality, if any", example = "Marylebone", nullable = true)
  val area: String?,

  @Schema(description = "City code, if any", example = "25343", nullable = true)
  val cityCode: String?,

  @Schema(description = "The description of the city code, if any", example = "Sheffield", nullable = true)
  val cityDescription: String?,

  @Schema(description = "County code, if any", example = "S.YORKSHIRE", nullable = true)
  val countyCode: String?,

  @Schema(description = "The description of county code, if any", example = "South Yorkshire", nullable = true)
  val countyDescription: String?,

  @Schema(description = "Postal code, if any", example = "NW1 6XE", nullable = true)
  val postcode: String?,

  @Schema(description = "Country code, if any", example = "ENG", nullable = true)
  val countryCode: String?,

  @Schema(description = "The description of country code, if any", example = "England", nullable = true)
  val countryDescription: String?,

  @Schema(description = "The business phone number for the primary address, if any", example = "01234 56789", nullable = true)
  val businessPhoneNumber: String?,

  @Schema(description = "The extension for the business phone number for the primary address, if any", example = "123", nullable = true)
  val businessPhoneNumberExtension: String?,
)
