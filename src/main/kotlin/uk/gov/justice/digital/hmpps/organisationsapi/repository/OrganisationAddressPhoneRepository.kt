package uk.gov.justice.digital.hmpps.organisationsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressPhoneEntity

@Repository
interface OrganisationAddressPhoneRepository : JpaRepository<OrganisationAddressPhoneEntity, Long> {

  fun findByOrganisationId(organisationId: Long): List<OrganisationAddressPhoneEntity>

  @Modifying
  @Query("delete from OrganisationAddressPhoneEntity o where o.organisationId = :organisationId")
  fun deleteAllByOrganisationId(organisationId: Long): Int
}
