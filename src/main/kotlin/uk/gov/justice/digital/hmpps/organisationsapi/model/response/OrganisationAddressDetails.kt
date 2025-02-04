package uk.gov.justice.digital.hmpps.organisationsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "An address related to an organisation with descriptions of all reference data")
data class OrganisationAddressDetails(

  @Schema(description = "The id of the organisation address", example = "123456")
  val organisationAddressId: Long,

  @Schema(description = "The id of the organisation", example = "123456")
  val organisationId: Long,

  @Schema(
    description =
    """
      The type of address (optional).
      This is a coded value (from the group code ADDRESS_TYPE in reference data).
      The known values are HOME, WORK or BUS (business address).
    """,
    example = "HOME",
    nullable = true,
  )
  val addressType: String?,

  @Schema(description = "The description of the address type", example = "HOME", nullable = true)
  val addressTypeDescription: String?,

  @Schema(description = "True if this is the primary address otherwise false", example = "true")
  val primaryAddress: Boolean,

  @Schema(description = "Flat number or name", example = "Flat 2B", nullable = true)
  val flat: String?,

  @Schema(description = "Building or house number or name", example = "Mansion House", nullable = true)
  val property: String?,

  @Schema(description = "Street or road name", example = "Acacia Avenue", nullable = true)
  val street: String?,

  @Schema(description = "Area", example = "Morton Heights", nullable = true)
  val area: String?,

  @Schema(description = "City code", example = "25343", nullable = true)
  val cityCode: String?,

  @Schema(description = "The description of city code", example = "Sheffield", nullable = true)
  val cityDescription: String?,

  @Schema(description = "County code", example = "S.YORKSHIRE", nullable = true)
  val countyCode: String?,

  @Schema(description = "The description of county code", example = "South Yorkshire", nullable = true)
  val countyDescription: String?,

  @Schema(description = "Postcode", example = "S13 4FH", nullable = true)
  val postcode: String?,

  @Schema(description = "Country code", example = "ENG", nullable = true)
  val countryCode: String?,

  @Schema(description = "The description of country code", example = "England", nullable = true)
  val countryDescription: String?,

  @Schema(description = "Flag to indicate whether mail is allowed to be sent to this address", example = "false")
  val mailAddress: Boolean,

  @Schema(description = "Flag to indicate whether the organisations service is provided at this address", example = "false")
  val serviceAddress: Boolean,

  @Schema(description = "The start date when this address is to be considered active from", example = "2024-01-01", nullable = true)
  val startDate: LocalDate?,

  @Schema(description = "The end date when this address is to be considered no longer active", example = "2024-01-01", nullable = true)
  val endDate: LocalDate?,

  @Schema(description = "Flag to indicate whether this address indicates no fixed address", example = "false")
  val noFixedAddress: Boolean,

  @Schema(description = "Any additional information or comments about the address", example = "Some additional information", nullable = true)
  val comments: String?,

  @Schema(
    description = "Special needs code for this address from reference data ORG_ADDRESS_SPECIAL_NEEDS.",
    nullable = true,
    example = "DEAF",
  )
  val specialNeedsCode: String?,

  @Schema(
    description = "The description of the special needs code",
    nullable = true,
    example = "Hearing Impaired Translation",
  )
  val specialNeedsCodeDescription: String?,

  @Schema(description = "The name of the contact person at this address", example = "Joe Bloggs", nullable = true)
  val contactPersonName: String?,

  @Schema(description = "The business hours of the address", example = "9-5", nullable = true)
  val businessHours: String?,

  @Schema(description = "Phone numbers associated with this address")
  val phoneNumbers: List<OrganisationAddressPhoneDetails>,

  @Schema(description = "The id of the user who created the entry", example = "JD000001")
  val createdBy: String,

  @Schema(description = "The timestamp of when the entry was created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime,

  @Schema(description = "The id of the user who last updated the entry", example = "JD000001", nullable = true)
  val updatedBy: String?,

  @Schema(description = "The timestamp of when the entry was last updated", example = "2024-01-01T00:00:00Z", nullable = true)
  val updatedTime: LocalDateTime?,
)
