package uk.gov.justice.digital.hmpps.organisationsapi.service.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service

@Service
class TelemetryService(private val telemetryClient: TelemetryClient) {
  fun trackEvent(
    eventType: TelemetryCustomEventType,
    properties: Map<String, String?>,
    metrics: Map<String, Double>? = null,
  ) {
    telemetryClient.trackEvent(
      eventType.eventName,
      properties.mapNotNull { (key, value) -> value?.let { key to it } }.toMap(),
      metrics,
    )
  }
}
