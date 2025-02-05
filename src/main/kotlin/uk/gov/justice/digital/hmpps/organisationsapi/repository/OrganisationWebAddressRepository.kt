package uk.gov.justice.digital.hmpps.organisationsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWebAddressEntity

@Repository
interface OrganisationWebAddressRepository : JpaRepository<OrganisationWebAddressEntity, Long> {
  fun findByOrganisationId(organisationId: Long): List<OrganisationWebAddressEntity>

  @Modifying
  @Query("delete from OrganisationWebAddressEntity o where o.organisationId = :organisationId")
  fun deleteAllByOrganisationId(organisationId: Long): Int
}
