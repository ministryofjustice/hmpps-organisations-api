package uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response object for email address changes via sync")
data class SyncOrganisationId(
  @Schema(description = "The ID for an organisation", example = "111111")
  val organisationId: Long,
)
