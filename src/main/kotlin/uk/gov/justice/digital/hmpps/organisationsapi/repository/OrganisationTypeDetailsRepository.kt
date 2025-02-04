package uk.gov.justice.digital.hmpps.organisationsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeDetailsEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeId

@Repository
interface OrganisationTypeDetailsRepository : JpaRepository<OrganisationTypeDetailsEntity, OrganisationTypeId> {
  fun findByIdOrganisationId(organisationId: Long): List<OrganisationTypeDetailsEntity>
}
