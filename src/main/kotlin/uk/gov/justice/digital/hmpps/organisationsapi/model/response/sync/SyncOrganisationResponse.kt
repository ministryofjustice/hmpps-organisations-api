package uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class SyncOrganisationResponse(
  @Schema(description = "The id of the organisation", example = "123456")
  val organisationId: Long,

  @Schema(description = "Organisation name", example = "Supplier Services plc", nullable = true)
  val organisationName: String? = null,

  @Schema(description = "Programme number", example = "8765", nullable = true)
  val programmeNumber: String? = null,

  @Schema(description = "VAT number", example = "GB 55 55 55 55", nullable = true)
  val vatNumber: String? = null,

  @Schema(description = "Caseload ID (a specific prison)", example = "HEI", nullable = true)
  val caseloadId: String? = null,

  @Schema(description = "Comments related to this organisation", example = "Notes", nullable = true)
  val comments: String? = null,

  @Schema(description = "Active flag", example = "true")
  var active: Boolean = false,

  @Schema(description = "The date this organisation was deactivated", example = "2019-01-01", nullable = true)
  val deactivatedDate: LocalDate? = null,

  @Schema(description = "User who created the organisation", example = "admin")
  val createdBy: String,

  @Schema(description = "Timestamp when the organisation was created", example = "2023-09-23T10:15:30")
  val createdTime: LocalDateTime,

  @Schema(description = "User who updated the organisation", example = "admin2")
  val updatedBy: String? = null,

  @Schema(description = "Timestamp when the organisation was updated", example = "2023-09-24T12:00:00")
  val updatedTime: LocalDateTime? = null,
)
