package uk.gov.justice.digital.hmpps.organisationsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.organisationsapi.model.ReferenceCodeGroup

@Schema(description = "Describes the details of a reference code")
data class ReferenceCode(

  @Schema(description = "An internally-generated unique identifier for this reference code.", example = "12345")
  val referenceCodeId: Long,

  @Schema(description = "The group name for related reference codes.", example = "PHONE_TYPE")
  val groupCode: ReferenceCodeGroup,

  @Schema(description = "The code for this reference data", example = "MOB")
  val code: String,

  @Schema(description = "A fuller description of the reference code", example = "Mobile")
  val description: String,

  @Schema(description = "The default order configured for the reference code, lowest number first.", example = "5")
  val displayOrder: Int,

  @Schema(description = "Whether the reference code is still in use. Old reference codes are maintained for compatability with legacy data.", example = "true")
  val isActive: Boolean,
)
