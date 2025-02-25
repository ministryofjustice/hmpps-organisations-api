package uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync

import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncOrganisationType
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncTypesResponse

fun List<OrganisationTypeEntity>.toModel(organisationId: Long) = SyncTypesResponse(
  organisationId = organisationId,
  types = this.map { each ->
    SyncOrganisationType(
      type = each.id.organisationType,
      createdBy = each.createdBy,
      createdTime = each.createdTime,
      updatedBy = each.updatedBy,
      updatedTime = each.updatedTime,
    )
  },
)
