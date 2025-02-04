package uk.gov.justice.digital.hmpps.organisationsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "A type categorizing the organisations")
data class OrganisationTypeDetails(
  @Schema(description = "Unique identifier for the organisation", example = "123")
  val organisationId: Long,

  @Schema(description = "Type of organisation", example = "TRUST")
  val organisationType: String,

  @Schema(description = "Description of the type of organisation", example = "Trust")
  val organisationTypeDescription: String,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "Timestamp when the entry was created", example = "2023-09-23T10:15:30")
  val createdTime: LocalDateTime,

  @Schema(description = "User who updated the entry", example = "admin2")
  val updatedBy: String?,

  @Schema(description = "Timestamp when the entry was updated", example = "2023-09-24T12:00:00")
  val updatedTime: LocalDateTime?,
)
