package uk.gov.justice.digital.hmpps.organisationsapi.service.telemetry

enum class TelemetryCustomEventType(val eventName: String, val description: String) {
  ORGANISATION_CREATED("organisations-api.organisation.created", "An organisation has been created"),
  ORGANISATION_UPDATED("organisations-api.organisation.updated", "An organisation has been updated"),
  ORGANISATION_DELETED("organisations-api.organisation.deleted", "An organisation has been deleted"),
}
