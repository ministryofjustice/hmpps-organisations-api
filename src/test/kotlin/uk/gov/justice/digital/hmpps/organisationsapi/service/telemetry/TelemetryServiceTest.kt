package uk.gov.justice.digital.hmpps.organisationsapi.service.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TelemetryServiceTest {
  private val telemetryClient: TelemetryClient = mock()
  private val telemetryService = TelemetryService(telemetryClient)

  @Test
  fun `should track event with populated properties only`() {
    telemetryService.trackEvent(
      eventType = TelemetryCustomEventType.ORGANISATION_CREATED,
      properties = mapOf(
        "organisationId" to "123",
        "source" to "NOMIS",
        "caseload" to null,
      ),
    )

    verify(telemetryClient).trackEvent(
      "organisations-api.organisation.created",
      mapOf(
        "organisationId" to "123",
        "source" to "NOMIS",
      ),
      null as Map<String, Double>?,
    )
  }

  @Test
  fun `should track event with metrics`() {
    telemetryService.trackEvent(
      eventType = TelemetryCustomEventType.ORGANISATION_UPDATED,
      properties = mapOf("organisationId" to "123"),
      metrics = mapOf("duration" to 12.3),
    )

    verify(telemetryClient).trackEvent(
      "organisations-api.organisation.updated",
      mapOf("organisationId" to "123"),
      mapOf("duration" to 12.3),
    )
  }
}
