package uk.gov.justice.digital.hmpps.organisationsapi.facade

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.organisationsapi.service.OrganisationService
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEventsService

@Service
class OrganisationFacade(
  private val organisationService: OrganisationService,
  private val outboundEventsService: OutboundEventsService,
) {
  fun create(request: CreateOrganisationRequest): OrganisationDetails = organisationService.create(request).also {
    outboundEventsService.send(
      outboundEvent = OutboundEvent.ORGANISATION_CREATED,
      organisationId = it.organisationId,
      identifier = it.organisationId,
    )
  }

  fun getOrganisationById(organisationId: Long): OrganisationDetails = organisationService.getOrganisationById(organisationId)

  fun getOrganisationSummaryById(organisationId: Long): OrganisationSummary = organisationService.getOrganisationSummaryById(organisationId)

  fun search(request: OrganisationSearchRequest, pageable: Pageable): PagedModel<OrganisationSummary> = organisationService.search(request, pageable)
}
