package uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

data class SyncUpdateOrganisationRequest(
  @Schema(description = "The organisation ID AKA the corporate ID from NOMIS", example = "1233323")
  @field:NotNull(message = "The  organisation ID must be present in this request")
  val organisationId: Long,

  @Schema(description = "The name of the organisation", example = "Example Limited", maxLength = 40)
  @field:Size(max = 40, message = "organisationName must be <= 40 characters")
  val organisationName: String,

  @Schema(
    description = "The programme number for the organisation, stored as FEI_NUMBER in NOMIS",
    example = "1",
    maxLength = 40,
    nullable = true,
  )
  @field:Size(max = 40, message = "programmeNumber must be <= 40 characters")
  val programmeNumber: String? = null,

  @Schema(description = "The VAT number for the organisation, if known", example = "123456", maxLength = 12, nullable = true)
  @field:Size(max = 12, message = "vatNumber must be <= 12 characters")
  val vatNumber: String? = null,

  @Schema(
    description = "The id of the caseload for this organisation, this is an agency id in NOMIS",
    example = "BXI",
    maxLength = 6,
    nullable = true,
  )
  @field:Size(max = 6, message = "caseloadId must be <= 6 characters")
  val caseloadId: String? = null,

  @Schema(description = "Any comments on the organisation", example = "Some additional info", maxLength = 240, nullable = true)
  @field:Size(max = 240, message = "comments must be <= 240 characters")
  val comments: String? = null,

  @Schema(description = "Whether the organisation is active or not", example = "true")
  val active: Boolean = false,

  @Schema(description = "The date the organisation was deactivated, EXPIRY_DATE in NOMIS", example = "2010-12-30", nullable = true)
  val deactivatedDate: LocalDate? = null,

  @Schema(description = "User who updated the entry", example = "admin2")
  val updatedBy: String,

  @Schema(description = "Timestamp when the entry was updated", example = "2023-09-24T12:00:00")
  val updatedTime: LocalDateTime,
)
