package uk.gov.justice.digital.hmpps.organisationsapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(enumAsRef = true)
enum class ReferenceCodeGroup(val displayName: String, val isDocumented: Boolean) {
  CITY("city", true),
  COUNTY("county", true),
  COUNTRY("country", true),
  ADDRESS_TYPE("address type", true),
  PHONE_TYPE("phone type", true),
  ORGANISATION_TYPE("organisation type", true),
  ORG_ADDRESS_SPECIAL_NEEDS("organisation address special needs code", true),
  TEST_TYPE("test type", false),
  TEST_SEQ_TYPE("test seq type", false),
}
