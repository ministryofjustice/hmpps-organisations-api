package uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate

import io.swagger.v3.oas.annotations.media.Schema

data class MigrateOrganisationResponse(

  @Schema(description = "The pair of IDs for this organisation in NOMIS")
  val organisation: IdPair,

  @Schema(description = "List of organisation types that were created")
  val organisationTypes: List<String> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for phone numbers")
  val phoneNumbers: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for email addresses")
  val emailAddresses: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for web addresses")
  val webAddresses: List<IdPair> = emptyList(),

  @Schema(description = "List of Nomis and DPS IDs for addresses")
  val addresses: List<MigratedOrganisationAddress> = emptyList(),
)

data class MigratedOrganisationAddress(
  @Schema(description = "The pair of IDs for this organisation address in NOMIS")
  val address: IdPair,

  @Schema(description = "List of Nomis and DPS IDs for email addresses")
  val phoneNumbers: List<IdPair> = emptyList(),
)

data class MigratedOrganisationType(
  @Schema(description = "The type of the organisation from reference data", example = "TRUST")
  val organisationType: String,

  @Schema(description = "The unique ID created in the DPS contacts service", example = "1234")
  val dpsId: Long,
)
