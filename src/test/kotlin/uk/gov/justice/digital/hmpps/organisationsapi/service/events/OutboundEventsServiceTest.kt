package uk.gov.justice.digital.hmpps.organisationsapi.service.events

import org.assertj.core.api.Assertions.within
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.organisationsapi.config.FeatureSwitches
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class OutboundEventsServiceTest {
  private val eventsPublisher: OutboundEventsPublisher = mock()
  private val featureSwitches: FeatureSwitches = mock()
  private val outboundEventsService = OutboundEventsService(eventsPublisher, featureSwitches)
  private val eventCaptor = argumentCaptor<OutboundHMPPSDomainEvent>()

  @Test
  fun `organisation created event with id 1 is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_CREATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_CREATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation.created",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation has been created",
    )
  }

  @Test
  fun `organisation updated event with id 1 is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_UPDATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_UPDATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation.updated",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation has been updated",
    )
  }

  @Test
  fun `organisation deleted event with id 1 is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_DELETED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_DELETED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation.deleted",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation has been deleted",
    )
  }

  @ParameterizedTest
  @EnumSource(OutboundEvent::class)
  fun `should trap exception sending event`(event: OutboundEvent) {
    featureSwitches.stub { on { isEnabled(event) } doReturn true }
    whenever(eventsPublisher.send(any())).thenThrow(RuntimeException("Boom!"))
    outboundEventsService.send(event, 1L, 1L)
    verify(eventsPublisher).send(any())
  }

  private fun verify(
    expectedEventType: String,
    expectedAdditionalInformation: AdditionalInformation,
    expectedOccurredAt: LocalDateTime = LocalDateTime.now(),
    expectedDescription: String,
  ) {
    verify(eventsPublisher).send(eventCaptor.capture())

    with(eventCaptor.firstValue) {
      assertThat(eventType).isEqualTo(expectedEventType)
      assertThat(additionalInformation).isEqualTo(expectedAdditionalInformation)
      assertThat(occurredAt).isCloseTo(expectedOccurredAt, within(60, ChronoUnit.SECONDS))
      assertThat(description).isEqualTo(expectedDescription)
    }

    verifyNoMoreInteractions(eventsPublisher)
  }
}
