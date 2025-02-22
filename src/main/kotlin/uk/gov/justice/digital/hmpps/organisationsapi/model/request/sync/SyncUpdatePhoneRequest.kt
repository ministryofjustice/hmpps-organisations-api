package uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Schema(description = "Request to update a phone number for an organisation")
data class SyncUpdatePhoneRequest(

  @Schema(description = "The organisation ID this this number is linked to", example = "1233323")
  @field:NotNull(message = "The  organisation ID must be present in this request")
  val organisationId: Long,

  @Schema(description = "Type of phone number", example = "MOB")
  val phoneType: String,

  @Schema(description = "Phone number", example = "+1234567890")
  @field:NotNull(message = "The phone number must be provided")
  val phoneNumber: String,

  @Schema(description = "Extension number", example = "123", nullable = true)
  val extNumber: String? = null,

  @Schema(description = "User who updated the entry", example = "admin")
  val updatedBy: String,

  @Schema(description = "The timestamp the phone number was updated", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime = LocalDateTime.now(),
)
