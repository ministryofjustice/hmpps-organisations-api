package uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Response object for email address changes via sync")
data class SyncEmailResponse(
  @Schema(description = "The ID for this email address", example = "111111")
  val organisationEmailId: Long,

  @Schema(description = "The organisation ID it is linked to", example = "1234")
  val organisationId: Long,

  @Schema(description = "Email address", example = "name@example.com")
  val emailAddress: String,

  @Schema(description = "Username who created", example = "admin")
  val createdBy: String,

  @Schema(description = "The timestamp created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Username who last updated", example = "admin2", nullable = true)
  val updatedBy: String? = null,

  @Schema(description = "Timestamp last updated", example = "2023-09-24T12:00:00", nullable = true)
  val updatedTime: LocalDateTime? = null,
)
