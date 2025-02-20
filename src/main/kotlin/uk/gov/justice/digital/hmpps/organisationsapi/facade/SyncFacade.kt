package uk.gov.justice.digital.hmpps.organisationsapi.facade

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncOrganisationService

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
  private val syncService: SyncOrganisationService,
  private val outboundEventsService: OutboundEventsService,
) {
  // ================================================================
  //  Organisations
  // ================================================================

  fun getOrganisationById(organisationId: Long) = syncService.getOrganisationById(organisationId)

  fun createOrganisation(request: SyncCreateOrganisationRequest) = syncService.createOrganisation(request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_CREATED,
        organisationId = it.organisationId,
        identifier = it.organisationId,
        source = Source.NOMIS,
      )
    }

  fun updateOrganisation(organisationId: Long, request: SyncUpdateOrganisationRequest) = syncService.updateOrganisation(organisationId, request)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_UPDATED,
        organisationId = organisationId,
        identifier = organisationId,
        source = Source.NOMIS,
      )
    }

  fun deleteOrganisation(organisationId: Long) = syncService.deleteOrganisation(organisationId)
    .also {
      outboundEventsService.send(
        outboundEvent = OutboundEvent.ORGANISATION_DELETED,
        organisationId = organisationId,
        identifier = organisationId,
        source = Source.NOMIS,
      )
    }
}
