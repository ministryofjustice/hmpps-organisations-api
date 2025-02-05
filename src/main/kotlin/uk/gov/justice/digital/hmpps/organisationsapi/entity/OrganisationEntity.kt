package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * If making changes to this class they should be reflected in BaseOrganisationEntity and OrganisationWithFixedIdEntity as well.
 *
 * Generates a new id using the identity column. If syncing from NOMIS you should use OrganisationWithFixedIdEntity
 * instead which will not use the sequence and instead provide a specific value from NOMIS corporate_id.
 *
 * DPS organisation_id is >= 20000000 and NOMIS corporate_id is < 20000000.
 */
@Entity
@Table(name = "organisation")
data class OrganisationEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val organisationId: Long? = null,
  override val organisationName: String,
  override val programmeNumber: String?,
  override val vatNumber: String?,
  override val caseloadId: String?,
  override val comments: String?,
  override val active: Boolean,
  override val deactivatedDate: LocalDate?,
  override val createdBy: String,
  override val createdTime: LocalDateTime,
  override val updatedBy: String?,
  override val updatedTime: LocalDateTime?,
) : BaseOrganisationEntity(
  organisationName = organisationName,
  programmeNumber = programmeNumber,
  vatNumber = vatNumber,
  caseloadId = caseloadId,
  comments = comments,
  active = active,
  deactivatedDate = deactivatedDate,
  createdBy = createdBy,
  createdTime = createdTime,
  updatedBy = updatedBy,
  updatedTime = updatedTime,
) {
  override fun id(): Long = requireNotNull(this.organisationId) { "Organisation id should be non-null once created" }
}
