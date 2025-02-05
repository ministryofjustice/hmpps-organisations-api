package uk.gov.justice.digital.hmpps.organisationsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEmailEntity

@Repository
interface OrganisationEmailRepository : JpaRepository<OrganisationEmailEntity, Long> {
  fun findByOrganisationId(organisationId: Long): List<OrganisationEmailEntity>

  @Modifying
  @Query("delete from OrganisationEmailEntity o where o.organisationId = :organisationId")
  fun deleteAllByOrganisationId(organisationId: Long): Int
}
