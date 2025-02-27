package uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request to update an organisation's address-specific phone number")
data class SyncUpdateAddressPhoneRequest(
  @Schema(description = "Type of phone", example = "MOB")
  val phoneType: String,

  @Schema(description = "Phone number", example = "+1234567890")
  val phoneNumber: String,

  @Schema(description = "Extension number", example = "123")
  val extNumber: String? = null,

  @Schema(description = "The username of the person who made the update", example = "JD000001")
  val updatedBy: String,

  @Schema(description = "The time when the update was made", example = "2024-01-01T00:00:00Z")
  val updatedTime: LocalDateTime,
)
