package uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "Request to migrate an organisation/corporate and all of its sub-elements from NOMIS into this service")
data class MigrateOrganisationRequest(
  @Schema(description = "The corporate ID from NOMIS", example = "1233323")
  @field:NotNull(message = "The NOMIS corporate ID must be present in the request")
  val nomisCorporateId: Long,

  @Schema(description = "The name of the organisation", example = "Example Limited", maxLength = 40)
  @field:Size(max = 40, message = "organisationName must be <= 40 characters")
  val organisationName: String,

  @Schema(
    description = "The programme number for the organisation, stored as FEI_NUMBER in NOMIS",
    example = "1",
    maxLength = 40,
  )
  @field:Size(max = 40, message = "programmeNumber must be <= 40 characters")
  val programmeNumber: String?,

  @Schema(description = "The VAT number for the organisation, if known", example = "123456", maxLength = 12)
  @field:Size(max = 12, message = "vatNumber must be <= 12 characters")
  val vatNumber: String?,

  @Schema(
    description = "The id of the caseload for this organisation, this is an agency id in NOMIS",
    example = "BXI",
    maxLength = 6,
  )
  @field:Size(max = 6, message = "caseloadId must be <= 6 characters")
  val caseloadId: String?,

  @Schema(description = "Any comments on the organisation", example = "Some additional info", maxLength = 240)
  @field:Size(max = 240, message = "comments must be <= 240 characters")
  val comments: String?,

  @Schema(description = "Whether the organisation is active or not", example = "true")
  val active: Boolean,

  @Schema(description = "The date the organisation was deactivated, EXPIRY_DATE in NOMIS", example = "2010-12-30")
  val deactivatedDate: LocalDate?,

  @Schema(description = "The types of the organisation, CORPORATE_TYPES in NOMIS.")
  val organisationTypes: List<MigrateOrganisationType>,

  @Schema(description = "Phone numbers associated directly with the organisation and it's addresses")
  val phoneNumbers: List<MigrateOrganisationPhoneNumber>,

  @Schema(description = "Emails associated with the organisation")
  val emailAddresses: List<MigrateOrganisationEmailAddress>,

  @Schema(description = "Web addresses associated with the organisation")
  val webAddresses: List<MigrateOrganisationWebAddress>,

  @Schema(description = "Addresses associated with the organisation")
  val addresses: List<MigrateOrganisationAddress>,
) : AbstractAuditable()

data class MigrateOrganisationType(
  @Schema(description = "Type of organisation from reference data", example = "TRUST")
  val type: String,
) : AbstractAuditable()

data class MigrateOrganisationPhoneNumber(
  @Schema(description = "Unique phone ID in NOMIS", example = "123")
  val nomisPhoneId: Long,

  @Schema(description = "Telephone number", example = "098989 98989893")
  @field:NotNull(message = "The phone number must be provided")
  val number: String,

  @Schema(description = "Extension number (optional)", nullable = true, example = "100")
  val extension: String? = null,

  @Schema(description = "Type of phone number (from reference data)")
  val type: String,
) : AbstractAuditable()

data class MigrateOrganisationEmailAddress(
  @Schema(description = "Unique email ID in NOMIS", example = "123")
  val nomisEmailAddressId: Long,

  @Schema(description = "Email address", example = "test@example.com")
  val email: String,
) : AbstractAuditable()

data class MigrateOrganisationWebAddress(
  @Schema(description = "Unique web address ID in NOMIS", example = "123")
  val nomisWebAddressId: Long,

  @Schema(description = "Email address", example = "www.example.com")
  val webAddress: String,
) : AbstractAuditable()

data class MigrateOrganisationAddress(
  @Schema(description = "Unique address ID in NOMIS", example = "123")
  val nomisAddressId: Long,

  @Schema(description = "Address type from reference data", nullable = true)
  val type: String? = null,

  @Schema(description = "Flat number or identifier", nullable = true, example = "1B")
  val flat: String? = null,

  @Schema(description = "House name or number", nullable = true, example = "43")
  val premise: String? = null,

  @Schema(description = "Street or road", nullable = true, example = "Main Street")
  val street: String? = null,

  @Schema(description = "Locality", nullable = true, example = "Keighley")
  val locality: String? = null,

  @Schema(description = "Postcode", nullable = true, example = "BD12 8RD")
  val postCode: String? = null,

  @Schema(description = "City - code from reference data", nullable = true)
  val city: String? = null,

  @Schema(description = "County - code from reference data", nullable = true)
  val county: String? = null,

  @Schema(description = "Country - code from reference data", nullable = true)
  val country: String? = null,

  @Schema(
    description = "If true this address should be considered as no fixed address",
    nullable = true,
    example = "false",
  )
  val noFixedAddress: Boolean = false,

  @Schema(
    description = "If true this address should be considered as the primary residential address",
    nullable = true,
    example = "true",
  )
  val primaryAddress: Boolean = false,

  @Schema(
    description = "If true this address should be considered for sending mail to",
    nullable = true,
    example = "true",
  )
  val mailAddress: Boolean = false,

  @Schema(
    description = "If this is the service address for the organisation",
    nullable = true,
    example = "true",
  )
  val serviceAddress: Boolean = false,

  @Schema(description = "Comments relating to this address", nullable = true, example = "A comment")
  val comment: String? = null,

  @Schema(
    description = "Special needs code for this address from SPECIAL_NEEDS in NOMIS.",
    nullable = true,
    example = "DEAF",
  )
  val specialNeedsCode: String? = null,

  @Schema(description = "The name of the contact person at this address", nullable = true, example = "Joe Bloggs")
  val contactPersonName: String? = null,

  @Schema(description = "The business hours for this address", nullable = true, example = "9-5")
  val businessHours: String? = null,

  @Schema(
    description = "The date this address should be considered valid from",
    nullable = true,
    example = "2018-10-01",
  )
  val startDate: LocalDate? = null,

  @Schema(description = "The date this address should be considered valid to", nullable = true, example = "2022-04-04")
  val endDate: LocalDate? = null,

  @Schema(description = "A list of phone numbers which are linked to this address")
  val phoneNumbers: List<MigrateOrganisationPhoneNumber> = emptyList(),
) : AbstractAuditable()
