package uk.gov.justice.digital.hmpps.organisationsapi.exception

import uk.gov.justice.digital.hmpps.organisationsapi.model.ReferenceCodeGroup

class InvalidReferenceCodeGroupException(requestedGroup: String) : Exception(""""$requestedGroup" is not a valid reference code group. Valid groups are ${ReferenceCodeGroup.entries.filter { it.isDocumented }.joinToString(", ")}""".trimIndent())
