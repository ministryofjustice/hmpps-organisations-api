package uk.gov.justice.digital.hmpps.organisationsapi.model.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Organisation search request query parameters")
data class OrganisationSearchRequest(

  @Schema(description = "Full or partial name of the organisation", example = "NHS", nullable = false, maxLength = 40)
  @field:NotBlank(message = "Name cannot be blank")
  val name: String,
)
