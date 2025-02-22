package uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Response object for phone number changes via sync")
data class SyncPhoneResponse(
  @Schema(description = "The ID for this telephone number", example = "111111")
  val organisationPhoneId: Long,

  @Schema(description = "The organisation ID this telephone number is linked with", example = "1234")
  val organisationId: Long,

  @Schema(description = "Type of phone number", example = "MOB")
  val phoneType: String,

  @Schema(description = "Phone number", example = "+1234567890")
  val phoneNumber: String,

  @Schema(description = "Extension number", example = "123", nullable = true)
  val extNumber: String? = null,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "The timestamp of when this phone number was created", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "User who last updated this phone number", example = "admin2", nullable = true)
  val updatedBy: String? = null,

  @Schema(description = "Timestamp last updated", example = "2023-09-24T12:00:00", nullable = true)
  val updatedTime: LocalDateTime? = null,
)
