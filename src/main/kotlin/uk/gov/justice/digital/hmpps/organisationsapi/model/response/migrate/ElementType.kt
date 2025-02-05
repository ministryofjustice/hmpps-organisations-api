package uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate

/**
 * Describes the valid type values for an IdPair object
 */
enum class ElementType(val elementType: String) {
  PHONE("Phone"),
  EMAIL("Email"),
  ADDRESS("Address"),
  ADDRESS_PHONE("AddressPhone"),
  ORGANISATION("Organisation"),
  WEB_ADDRESS("WebAddress"),
}
