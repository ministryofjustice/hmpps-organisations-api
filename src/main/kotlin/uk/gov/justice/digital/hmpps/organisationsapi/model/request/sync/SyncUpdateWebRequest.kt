package uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Schema(description = "Request to update a web address for an organisation")
data class SyncUpdateWebRequest(

  @Schema(description = "The organisation ID linked to", example = "1233323")
  @field:NotNull(message = "The  organisation ID must be present in this request")
  val organisationId: Long,

  @Schema(description = "Web address", example = "www.example.com")
  @field:NotNull(message = "The web address must be provided")
  val webAddress: String,

  @Schema(description = "User who updated the entry", example = "admin")
  val updatedBy: String,

  @Schema(description = "The update timestamp", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime = LocalDateTime.now(),
)
