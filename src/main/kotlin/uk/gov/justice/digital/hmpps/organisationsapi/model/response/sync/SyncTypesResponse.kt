package uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncOrganisationType

@Schema(description = "Response object for an organisation types update")
data class SyncTypesResponse(
  @Schema(description = "The organisation ID the updates refer to", example = "1234")
  val organisationId: Long,

  @Schema(description = "The list of types updated on the organisation")
  val types: List<SyncOrganisationType>,
)
