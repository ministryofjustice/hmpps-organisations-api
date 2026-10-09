package uk.gov.justice.digital.hmpps.organisationsapi.model.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Request to create an organisation with all its legacy NOMIS data")
data class OrganisationV2CreateRequest(
  @Schema(description = "The name of the organisation", example = "Example Limited", maxLength = 40)
  @field:NotBlank(message = "organisationName must not be blank")
  @field:Size(max = 40, message = "organisationName must be <= 40 characters")
  val organisationName: String,

  @Schema(description = "Programme number in NOMIS", example = "FEI-12345", maxLength = 40, nullable = true)
  @field:Size(max = 40, message = "programmeNumber must be <= 40 characters")
  val programmeNumber: String?,

  @Schema(description = "VAT number", example = "GB123456789", maxLength = 12, nullable = true)
  @field:Size(max = 12, message = "vatNumber must be <= 12 characters")
  val vatNumber: String?,

  @Schema(description = "Caseload ID", example = "BXI", maxLength = 6, nullable = true)
  @field:Size(max = 6, message = "caseloadId must be <= 6 characters")
  val caseloadId: String?,

  @Schema(description = "Comments about the organisation", maxLength = 240, nullable = true)
  @field:Size(max = 240, message = "comments must be <= 240 characters")
  val comments: String?,

  @Schema(description = "Organisation types")
  val organisationTypes: List<
    @NotBlank
    @Size(max = 12)
    String,
    >,

  @Schema(description = "Whether the organisation is active")
  val active: Boolean,

  @Schema(description = "Date the organisation was deactivated", nullable = true)
  val deactivatedDate: LocalDate?,

  @field:Valid
  @Schema(description = "Phone numbers associated directly with the organisation")
  val phoneNumbers: List<OrganisationV2PhoneRequest>,

  @field:Valid
  @Schema(description = "Email and web addresses")
  val internetAddresses: List<OrganisationV2InternetAddressRequest>,

  @field:Valid
  @Schema(description = "Addresses associated with the organisation")
  val addresses: List<OrganisationV2AddressRequest>,

  @Schema(description = "User who created the entry", example = "admin")
  @field:NotBlank(message = "createdBy must not be blank")
  @field:Size(max = 100, message = "createdBy must be <= 100 characters")
  val createdBy: String,

  @Schema(description = "Timestamp when the entry was created", example = "2023-09-23T10:15:30")
  val createdTime: LocalDateTime,

  @Schema(description = "User who updated the entry", example = "admin2", nullable = true)
  val updatedBy: String?,

  @Schema(description = "Timestamp when the entry was updated", example = "2023-09-24T12:00:00", nullable = true)
  val updatedTime: LocalDateTime?,
) {
  @get:JsonIgnore
  @get:Schema(hidden = true)
  @get:AssertTrue(message = "deactivatedDate can only be supplied for an inactive organisation")
  val isDeactivatedDateConsistent: Boolean
    get() = !active || deactivatedDate == null

  @get:JsonIgnore
  @get:Schema(hidden = true)
  @get:AssertTrue(message = "At most one primary address and one mail address are allowed")
  val isPrimaryAndMailAddressesUnique: Boolean
    get() = addresses.count { it.primaryAddress } <= 1 && addresses.count { it.mailAddress } <= 1

  @get:JsonIgnore
  @get:Schema(hidden = true)
  @get:AssertTrue(message = "organisationTypes must not contain duplicates")
  val isOrganisationTypesUnique: Boolean
    get() = organisationTypes.distinct().size == organisationTypes.size
}

data class OrganisationV2PhoneRequest(
  @Schema(description = "Phone type from reference data", example = "BUS", maxLength = 12)
  @field:NotBlank(message = "phoneType must not be blank")
  @field:Size(max = 12, message = "phoneType must be <= 12 characters")
  val phoneType: String,

  @Schema(description = "Phone number", example = "01632 960123", maxLength = 40)
  @field:NotBlank(message = "phoneNumber must not be blank")
  @field:Size(max = 40, message = "phoneNumber must be <= 40 characters")
  @field:Pattern(regexp = "^[0-9 ()+\\-]*$", message = "phoneNumber may only contain digits, spaces and ( ) + -")
  val phoneNumber: String,

  @Schema(description = "Phone extension", example = "100", nullable = true, maxLength = 7)
  @field:Size(max = 7, message = "extNumber must be <= 7 characters")
  @field:Pattern(regexp = "^[0-9]*$", message = "extNumber may only contain digits")
  val extNumber: String?,
)

data class OrganisationV2InternetAddressRequest(
  @Schema(description = "Internet address class", example = "EMAIL", allowableValues = ["EMAIL", "WEB"])
  @field:Pattern(regexp = "EMAIL|WEB", message = "type must be EMAIL or WEB")
  val type: String,

  @Schema(description = "Email or web address", example = "contact@example.org", maxLength = 240)
  @field:NotBlank(message = "address must not be blank")
  @field:Size(max = 240, message = "address must be <= 240 characters")
  val address: String,
)

data class OrganisationV2AddressRequest(
  @Schema(description = "No fixed address")
  val noFixedAddress: Boolean,

  @Schema(description = "Flat or unit", nullable = true, maxLength = 30)
  @field:Size(max = 30, message = "flat must be <= 30 characters")
  val flat: String?,

  @Schema(description = "Premises or property", nullable = true, maxLength = 135)
  @field:Size(max = 135, message = "property must be <= 135 characters")
  val property: String?,

  @Schema(description = "Street", nullable = true, maxLength = 160)
  @field:Size(max = 160, message = "street must be <= 160 characters")
  val street: String?,

  @Schema(description = "Locality or area", nullable = true, maxLength = 70)
  @field:Size(max = 70, message = "area must be <= 70 characters")
  val area: String?,

  @Schema(description = "City reference code", nullable = true, maxLength = 12)
  @field:Size(max = 12, message = "cityCode must be <= 12 characters")
  val cityCode: String?,

  @Schema(description = "County reference code", nullable = true, maxLength = 12)
  @field:Size(max = 12, message = "countyCode must be <= 12 characters")
  val countyCode: String?,

  @Schema(description = "Postcode", nullable = true, maxLength = 12)
  @field:Size(max = 12, message = "postCode must be <= 12 characters")
  val postCode: String?,

  @Schema(description = "Country reference code", nullable = true, maxLength = 12)
  @field:Size(max = 12, message = "countryCode must be <= 12 characters")
  val countryCode: String?,

  @Schema(description = "Date this address became valid", nullable = true)
  val startDate: LocalDate?,

  @Schema(description = "Date this address stopped being valid", nullable = true)
  val endDate: LocalDate?,

  @Schema(description = "Address comments", nullable = true, maxLength = 240)
  @field:Size(max = 240, message = "comments must be <= 240 characters")
  val comments: String?,

  @Schema(
    description = "Address type reference code",
    nullable = true,
    maxLength = 12,
    allowableValues = ["HOME", "WORK", "BUS"],
  )
  @field:Size(max = 12, message = "addressType must be <= 12 characters")
  val addressType: String?,

  @Schema(description = "Whether this is a service address")
  val serviceAddress: Boolean,

  @Schema(description = "Special needs reference code", nullable = true, maxLength = 12)
  @field:Size(max = 12, message = "specialNeedsCode must be <= 12 characters")
  val specialNeedsCode: String?,

  @Schema(description = "Whether this is the primary address")
  val primaryAddress: Boolean,

  @Schema(description = "Whether mail can be sent to this address")
  val mailAddress: Boolean,

  @Schema(description = "Contact person", nullable = true, maxLength = 40)
  @field:Size(max = 40, message = "contactPersonName must be <= 40 characters")
  val contactPersonName: String?,

  @Schema(description = "Business hours", nullable = true, maxLength = 60)
  @field:Size(max = 60, message = "businessHours must be <= 60 characters")
  val businessHours: String?,

  @field:Valid
  @Schema(description = "Phone numbers associated with this address")
  val phoneNumbers: List<OrganisationV2PhoneRequest>,
) {
  @get:JsonIgnore
  @get:Schema(hidden = true)
  @get:AssertTrue(message = "endDate must not be before startDate")
  val isDatesConsistent: Boolean
    get() = startDate == null || endDate == null || !endDate.isBefore(startDate)
}
