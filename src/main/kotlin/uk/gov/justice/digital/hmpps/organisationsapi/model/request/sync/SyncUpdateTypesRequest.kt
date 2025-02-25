package uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Schema(description = "Request to update the list of types assigned to an organisation")
data class SyncUpdateTypesRequest(

  @Schema(description = "The organisation ID to update", example = "1233323")
  @field:NotNull(message = "The organisation ID must be present in this request")
  val organisationId: Long,

  @Schema(description = "The list of organisation types (will replace the current types)")
  val types: List<SyncOrganisationType> = emptyList(),
)

@Schema(description = "An organisation type")
data class SyncOrganisationType(
  @Schema(description = "Organisation type", example = "SUPPLIER")
  val type: String,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "The created timestamp", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "User who updated the entry", example = "admin")
  val updatedBy: String? = null,

  @Schema(description = "The updated timestamp", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime? = null,
)
