package uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Response object for web address changes via sync")
data class SyncWebResponse(
  @Schema(description = "The ID for this web address", example = "111111")
  val organisationWebAddressId: Long,

  @Schema(description = "The organisation ID it is linked to", example = "1234")
  val organisationId: Long,

  @Schema(description = "Web address", example = "www.example.com")
  val webAddress: String,

  @Schema(description = "Username who created", example = "admin")
  val createdBy: String,

  @Schema(description = "The timestamp created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Username who last updated", example = "admin2", nullable = true)
  val updatedBy: String? = null,

  @Schema(description = "Timestamp last updated", example = "2023-09-24T12:00:00", nullable = true)
  val updatedTime: LocalDateTime? = null,
)
