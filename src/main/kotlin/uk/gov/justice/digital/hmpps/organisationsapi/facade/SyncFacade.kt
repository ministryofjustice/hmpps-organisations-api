package uk.gov.justice.digital.hmpps.organisationsapi.facade

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateTypesRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncAddressPhoneService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncAddressService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncEmailService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncOrganisationService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncPhoneService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncTypesService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncWebService

/**
 * This class is a facade over the sync services as a thin layer
 * which is called by the sync controllers and in-turn calls the sync
 * service methods.
 *
 * Each method provides two purposes:
 * - To call the underlying sync services and apply the changes in a transactional method.
 * - To generate a domain event to inform subscribed services what has happened.
 *
 * All events generated as a result of a sync operation should generate domain events with the
 * additionalInformation.source = "NOMIS" to indicate that the actual source of the change
 * was NOMIS.
 *
 * This is important as the Syscon sync service will ignore domain events with
 * a source of NOMIS, but will action those with a source of DPS for changes which
 * originate within this service via the UI or API clients.
 */

@Service
class SyncFacade(
  private val syncOrganisationService: SyncOrganisationService,
  private val syncPhoneService: SyncPhoneService,
  private val syncEmailService: SyncEmailService,
  private val syncWebService: SyncWebService,
  private val syncAddressService: SyncAddressService,
  private val syncAddressPhoneService: SyncAddressPhoneService,
  private val syncTypesService: SyncTypesService,
  private val outboundEventsService: OutboundEventsService,
) {
  // ================================================================
  //  Organisations
  // ================================================================

  fun getOrganisationById(organisationId: Long) = syncOrganisationService.getOrganisationById(organisationId)

  fun createOrganisation(request: SyncCreateOrganisationRequest) = syncOrganisationService.createOrganisation(request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_CREATED,
        organisationId = it.organisationId,
        identifier = it.organisationId,
        source = Source.NOMIS,
      )
    }

  fun updateOrganisation(organisationId: Long, request: SyncUpdateOrganisationRequest) = syncOrganisationService.updateOrganisation(organisationId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_UPDATED,
        organisationId = organisationId,
        identifier = organisationId,
        source = Source.NOMIS,
      )
    }

  fun deleteOrganisation(organisationId: Long) = syncOrganisationService.deleteOrganisation(organisationId)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_DELETED,
        organisationId = organisationId,
        identifier = organisationId,
        source = Source.NOMIS,
      )
    }

  fun getIds(pageable: Pageable) = syncOrganisationService.getOrganisationIds(pageable)

  // ================================================================
  //  Organisation phone numbers
  // ================================================================

  fun getPhoneById(organisationPhoneId: Long) = syncPhoneService.getPhoneById(organisationPhoneId)

  fun createPhone(request: SyncCreatePhoneRequest) = syncPhoneService.createPhone(request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_PHONE_CREATED,
        organisationId = it.organisationId,
        identifier = it.organisationPhoneId,
        source = Source.NOMIS,
      )
    }

  fun updatePhone(organisationPhoneId: Long, request: SyncUpdatePhoneRequest) = syncPhoneService.updatePhone(organisationPhoneId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_PHONE_UPDATED,
        organisationId = it.organisationId,
        identifier = it.organisationPhoneId,
        source = Source.NOMIS,
      )
    }

  fun deletePhone(organisationPhoneId: Long) = syncPhoneService.deletePhone(organisationPhoneId)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_PHONE_DELETED,
        organisationId = it.organisationId,
        identifier = it.organisationPhoneId,
        source = Source.NOMIS,
      )
    }

  // ================================================================
  //  Organisation email
  // ================================================================

  fun getEmailById(organisationEmailId: Long) = syncEmailService.getEmailById(organisationEmailId)

  fun createEmail(request: SyncCreateEmailRequest) = syncEmailService.createEmail(request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_EMAIL_CREATED,
        organisationId = it.organisationId,
        identifier = it.organisationEmailId,
        source = Source.NOMIS,
      )
    }

  fun updateEmail(organisationEmailId: Long, request: SyncUpdateEmailRequest) = syncEmailService.updateEmail(organisationEmailId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_EMAIL_UPDATED,
        organisationId = it.organisationId,
        identifier = it.organisationEmailId,
        source = Source.NOMIS,
      )
    }

  fun deleteEmail(organisationEmailId: Long) = syncEmailService.deleteEmail(organisationEmailId)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_EMAIL_DELETED,
        organisationId = it.organisationId,
        identifier = it.organisationEmailId,
        source = Source.NOMIS,
      )
    }

  // ================================================================
  //  Organisation web address
  // ================================================================

  fun getWebById(organisationWebId: Long) = syncWebService.getWebAddressById(organisationWebId)

  fun createWeb(request: SyncCreateWebRequest) = syncWebService.createWeb(request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_WEB_CREATED,
        organisationId = it.organisationId,
        identifier = it.organisationWebAddressId,
        source = Source.NOMIS,
      )
    }

  fun updateWeb(organisationWebId: Long, request: SyncUpdateWebRequest) = syncWebService.updateWeb(organisationWebId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_WEB_UPDATED,
        organisationId = it.organisationId,
        identifier = it.organisationWebAddressId,
        source = Source.NOMIS,
      )
    }

  fun deleteWeb(organisationWebId: Long) = syncWebService.deleteWeb(organisationWebId)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_WEB_DELETED,
        organisationId = it.organisationId,
        identifier = it.organisationWebAddressId,
        source = Source.NOMIS,
      )
    }

  // ================================================================
  //  Organisation address
  // ================================================================

  fun getAddressById(organisationAddressId: Long) = syncAddressService.getAddressById(organisationAddressId)

  fun createAddress(request: SyncCreateAddressRequest) = syncAddressService.createAddress(request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_CREATED,
        organisationId = it.organisationId,
        identifier = it.organisationAddressId,
        source = Source.NOMIS,
      )
    }

  fun updateAddress(organisationAddressId: Long, request: SyncUpdateAddressRequest) = syncAddressService.updateAddress(organisationAddressId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_UPDATED,
        organisationId = it.organisationId,
        identifier = it.organisationAddressId,
        source = Source.NOMIS,
      )
    }

  fun deleteAddress(organisationAddressId: Long) = syncAddressService.deleteAddress(organisationAddressId)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_DELETED,
        organisationId = it.organisationId,
        identifier = it.organisationAddressId,
        source = Source.NOMIS,
      )
    }

  // ================================================================
  //  Organisation address phone numbers
  // ================================================================

  fun getAddressPhoneById(organisationAddressPhoneId: Long) = syncAddressPhoneService.getAddressPhoneById(organisationAddressPhoneId)

  fun createAddressPhone(request: SyncCreateAddressPhoneRequest) = syncAddressPhoneService.createAddressPhone(request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_PHONE_CREATED,
        organisationId = it.organisationId,
        identifier = it.organisationAddressPhoneId,
        source = Source.NOMIS,
      )
    }

  fun updateAddressPhone(organisationAddressPhoneId: Long, request: SyncUpdateAddressPhoneRequest) = syncAddressPhoneService.updateAddressPhone(organisationAddressPhoneId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_PHONE_UPDATED,
        organisationId = it.organisationId,
        identifier = it.organisationAddressPhoneId,
        source = Source.NOMIS,
      )
    }

  fun deleteAddressPhone(organisationAddressPhoneId: Long) = syncAddressPhoneService.deleteAddressPhone(organisationAddressPhoneId)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_PHONE_DELETED,
        organisationId = it.organisationId,
        identifier = it.organisationAddressPhoneId,
        source = Source.NOMIS,
      )
    }

  // ================================================================
  //  Organisation types
  // ================================================================

  fun getTypesByOrganisationId(organisationId: Long) = syncTypesService.getTypesByOrganisationId(organisationId)

  fun updateTypes(organisationId: Long, request: SyncUpdateTypesRequest) = syncTypesService.updateTypes(organisationId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_TYPES_UPDATED,
        organisationId = it.organisationId,
        identifier = it.organisationId,
        source = Source.NOMIS,
      )
    }
}
