package uk.gov.justice.digital.hmpps.organisationsapi.service.events

interface OutboundEventsPublisher {
  fun send(event: OutboundHMPPSDomainEvent)
}
