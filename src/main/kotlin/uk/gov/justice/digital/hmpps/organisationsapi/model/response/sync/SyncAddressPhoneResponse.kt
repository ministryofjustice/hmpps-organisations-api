package uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "An organisation's address-specific phone number")
data class SyncAddressPhoneResponse(
  @Schema(description = "Unique identifier for the address-specific phone number", example = "1")
  val organisationAddressPhoneId: Long,

  @Schema(description = "Unique identifier for the address to which this phone number is linked", example = "1")
  val organisationAddressId: Long,

  @Schema(description = "Unique identifier for the phone record", example = "1")
  val organisationPhoneId: Long,

  @Schema(description = "Unique identifier for the organisation linked to this address", example = "1")
  val organisationId: Long,

  @Schema(description = "Type of phone", example = "MOB")
  val phoneType: String,

  @Schema(description = "Phone number", example = "+1234567890")
  val phoneNumber: String,

  @Schema(description = "Extension number", example = "123")
  val extNumber: String?,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "Timestamp when the entry was created", example = "2023-09-23T10:15:30")
  val createdTime: LocalDateTime,

  @Schema(description = "User who updated the entry", example = "admin2")
  val updatedBy: String? = null,

  @Schema(description = "Timestamp when the entry was updated", example = "2023-09-24T12:00:00")
  val updatedTime: LocalDateTime? = null,
)
