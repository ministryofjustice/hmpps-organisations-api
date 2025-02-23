package uk.gov.justice.digital.hmpps.organisationsapi.service.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.organisationsapi.config.FeatureSwitches

@Service
class OutboundEventsService(
  private val publisher: OutboundEventsPublisher,
  private val featureSwitches: FeatureSwitches,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun send(
    outboundEvent: OutboundEvent,
    organisationId: Long,
    identifier: Long,
    source: Source = Source.DPS,
  ) {
    if (featureSwitches.isEnabled(outboundEvent)) {
      log.info("Sending outbound event $outboundEvent with source $source for identifier $identifier")

      when (outboundEvent) {
        OutboundEvent.ORGANISATION_CREATED,
        OutboundEvent.ORGANISATION_UPDATED,
        OutboundEvent.ORGANISATION_DELETED,
        OutboundEvent.ORGANISATION_PHONE_CREATED,
        OutboundEvent.ORGANISATION_PHONE_UPDATED,
        OutboundEvent.ORGANISATION_PHONE_DELETED,
        OutboundEvent.ORGANISATION_EMAIL_CREATED,
        OutboundEvent.ORGANISATION_EMAIL_UPDATED,
        OutboundEvent.ORGANISATION_EMAIL_DELETED,
        OutboundEvent.ORGANISATION_WEB_CREATED,
        OutboundEvent.ORGANISATION_WEB_UPDATED,
        OutboundEvent.ORGANISATION_WEB_DELETED,
        -> {
          sendSafely(outboundEvent, OrganisationInfo(organisationId, identifier, source))
        }
      }
    } else {
      log.warn("Outbound event type $outboundEvent feature is configured off.")
    }
  }

  private fun sendSafely(outboundEvent: OutboundEvent, additionalInformation: AdditionalInformation) {
    try {
      publisher.send(outboundEvent.event(additionalInformation))
    } catch (e: Exception) {
      log.error(
        "Unable to send event with type {}, info {}",
        outboundEvent,
        additionalInformation,
        e,
      )
    }
  }
}
