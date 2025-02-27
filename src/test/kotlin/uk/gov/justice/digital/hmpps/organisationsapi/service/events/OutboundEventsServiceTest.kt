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

  // ============= Organisations ==================

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

  // ============= Phone numbers ==================

  @Test
  fun `phone number created event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_PHONE_CREATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_PHONE_CREATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-phone.created",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation phone number has been created",
    )
  }

  @Test
  fun `phone number updated event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_PHONE_UPDATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_PHONE_UPDATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-phone.updated",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation phone number has been updated",
    )
  }

  @Test
  fun `phone number deleted event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_PHONE_DELETED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_PHONE_DELETED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-phone.deleted",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation phone number has been deleted",
    )
  }

  // ============= Email ==================

  @Test
  fun `email address created event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_EMAIL_CREATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_EMAIL_CREATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-email.created",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation email address has been created",
    )
  }

  @Test
  fun `email address updated event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_EMAIL_UPDATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_EMAIL_UPDATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-email.updated",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation email address has been updated",
    )
  }

  @Test
  fun `email address deleted event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_EMAIL_DELETED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_EMAIL_DELETED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-email.deleted",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation email address has been deleted",
    )
  }

  // ============= Web address ==================

  @Test
  fun `web address created event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_WEB_CREATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_WEB_CREATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-web.created",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation web address has been created",
    )
  }

  @Test
  fun `web address updated event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_WEB_UPDATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_WEB_UPDATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-web.updated",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation web address has been updated",
    )
  }

  @Test
  fun `web address deleted event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_WEB_DELETED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_WEB_DELETED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-web.deleted",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation web address has been deleted",
    )
  }

  // ============= Addresses ==================

  @Test
  fun `address created event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_ADDRESS_CREATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_ADDRESS_CREATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-address.created",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation address has been created",
    )
  }

  @Test
  fun `address updated event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_ADDRESS_UPDATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_ADDRESS_UPDATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-address.updated",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation address has been updated",
    )
  }

  @Test
  fun `address deleted event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_ADDRESS_DELETED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_ADDRESS_DELETED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-address.deleted",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation address has been deleted",
    )
  }

  // ============= Address-linked phone numbers ==================

  @Test
  fun `address phone created event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_ADDRESS_PHONE_CREATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_ADDRESS_PHONE_CREATED, 1L, 2L)
    verify(
      expectedEventType = "organisations-api.organisation-address-phone.created",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 2L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation address phone number has been created",
    )
  }

  @Test
  fun `address phone updated event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_ADDRESS_PHONE_UPDATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_ADDRESS_PHONE_UPDATED, 1L, 2L)
    verify(
      expectedEventType = "organisations-api.organisation-address-phone.updated",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 2L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation address phone number has been updated",
    )
  }

  @Test
  fun `address phone deleted event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_ADDRESS_PHONE_DELETED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_ADDRESS_PHONE_DELETED, 1L, 2L)
    verify(
      expectedEventType = "organisations-api.organisation-address-phone.deleted",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 2L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation address phone number has been deleted",
    )
  }

  // ============= Organisation types =============

  @Test
  fun `organisation types update event is sent to the events publisher`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.ORGANISATION_TYPES_UPDATED) } doReturn true }
    outboundEventsService.send(OutboundEvent.ORGANISATION_TYPES_UPDATED, 1L, 1L)
    verify(
      expectedEventType = "organisations-api.organisation-types.updated",
      expectedAdditionalInformation = OrganisationInfo(
        organisationId = 1L,
        identifier = 1L,
        source = Source.DPS,
      ),
      expectedDescription = "An organisation has had its types updated",
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
