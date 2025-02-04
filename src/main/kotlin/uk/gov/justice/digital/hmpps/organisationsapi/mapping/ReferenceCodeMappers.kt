package uk.gov.justice.digital.hmpps.organisationsapi.mapping

import uk.gov.justice.digital.hmpps.organisationsapi.entity.ReferenceCodeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.ReferenceCode

fun ReferenceCodeEntity.toModel() = ReferenceCode(
  referenceCodeId = referenceCodeId,
  groupCode = groupCode,
  code = code,
  description = description,
  displayOrder = displayOrder,
  isActive = isActive,
)

fun List<ReferenceCodeEntity>.toModel() = map { it.toModel() }
