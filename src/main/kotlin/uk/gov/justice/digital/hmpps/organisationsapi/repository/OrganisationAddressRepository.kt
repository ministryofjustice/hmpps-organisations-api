package uk.gov.justice.digital.hmpps.organisationsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressEntity

@Repository
interface OrganisationAddressRepository : JpaRepository<OrganisationAddressEntity, Long> {
  @Modifying
  @Query("delete from OrganisationAddressEntity o where o.organisationId = :organisationId")
  fun deleteAllByOrganisationId(organisationId: Long): Int
}
