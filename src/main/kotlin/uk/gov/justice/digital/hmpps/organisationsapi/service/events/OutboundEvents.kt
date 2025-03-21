package uk.gov.justice.digital.hmpps.organisationsapi.service.events

import java.time.LocalDateTime

/**
 * An enum class containing all events that can be raised from the service.
 * Each can tailor its own AdditionalInformation content.
 */
enum class OutboundEvent(val eventType: String) {
  ORGANISATION_CREATED("organisations-api.organisation.created") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation has been created",
    )
  },
  ORGANISATION_UPDATED("organisations-api.organisation.updated") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation has been updated",
    )
  },
  ORGANISATION_DELETED("organisations-api.organisation.deleted") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation has been deleted",
    )
  },
  ORGANISATION_PHONE_CREATED("organisations-api.organisation-phone.created") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation phone number has been created",
    )
  },
  ORGANISATION_PHONE_UPDATED("organisations-api.organisation-phone.updated") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation phone number has been updated",
    )
  },
  ORGANISATION_PHONE_DELETED("organisations-api.organisation-phone.deleted") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation phone number has been deleted",
    )
  },
  ORGANISATION_EMAIL_CREATED("organisations-api.organisation-email.created") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation email address has been created",
    )
  },
  ORGANISATION_EMAIL_UPDATED("organisations-api.organisation-email.updated") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation email address has been updated",
    )
  },
  ORGANISATION_EMAIL_DELETED("organisations-api.organisation-email.deleted") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation email address has been deleted",
    )
  },
  ORGANISATION_WEB_CREATED("organisations-api.organisation-web.created") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation web address has been created",
    )
  },
  ORGANISATION_WEB_UPDATED("organisations-api.organisation-web.updated") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation web address has been updated",
    )
  },
  ORGANISATION_WEB_DELETED("organisations-api.organisation-web.deleted") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation web address has been deleted",
    )
  },
  ORGANISATION_ADDRESS_CREATED("organisations-api.organisation-address.created") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation address has been created",
    )
  },
  ORGANISATION_ADDRESS_UPDATED("organisations-api.organisation-address.updated") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation address has been updated",
    )
  },
  ORGANISATION_ADDRESS_DELETED("organisations-api.organisation-address.deleted") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation address has been deleted",
    )
  },
  ORGANISATION_ADDRESS_PHONE_CREATED("organisations-api.organisation-address-phone.created") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation address phone number has been created",
    )
  },
  ORGANISATION_ADDRESS_PHONE_UPDATED("organisations-api.organisation-address-phone.updated") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation address phone number has been updated",
    )
  },
  ORGANISATION_ADDRESS_PHONE_DELETED("organisations-api.organisation-address-phone.deleted") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation address phone number has been deleted",
    )
  },
  ORGANISATION_TYPES_UPDATED("organisations-api.organisation-types.updated") {
    override fun event(additionalInformation: AdditionalInformation) = OutboundHMPPSDomainEvent(
      eventType = eventType,
      additionalInformation = additionalInformation,
      description = "An organisation has had its types updated",
    )
  },
  ;

  abstract fun event(additionalInformation: AdditionalInformation): OutboundHMPPSDomainEvent
}

/**
 * Base class for the additional information within events.
 * This is inherited and expanded individually for each event type.
 */

open class AdditionalInformation(open val source: Source)

/**
 * The class representing outbound domain events
 */
data class OutboundHMPPSDomainEvent(
  val eventType: String,
  val additionalInformation: AdditionalInformation,
  val version: String = "1",
  val description: String,
  val occurredAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * These are classes which define the different event content for AdditionalInformation.
 * All inherit the base class AdditionalInformation and extend it to contain the required fields.
 * The additional information is mapped into JSON by the ObjectMapper as part of the event body.
 */
data class OrganisationInfo(val organisationId: Long, val identifier: Long, override val source: Source = Source.DPS) : AdditionalInformation(source)

/**
 * The event source.
 * When data is changed within the DPS Contacts service by UI action or local process, events will have the source DPS.
 * When data is changed as a result of receiving a sync event, events will have the source NOMIS.
 */
enum class Source { DPS, NOMIS }
