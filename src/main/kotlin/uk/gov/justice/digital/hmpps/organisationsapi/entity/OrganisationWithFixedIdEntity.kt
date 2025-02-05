package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * If making changes to this class they should be reflected in BaseOrganisationEntity and OrganisationEntity as well.
 *
 * The id is manually specified using NOMIS corporate_id. If generating a new organisation on DPS you should use OrganisationEntity
 * instead which will generate a new id automatically.
 *
 * DPS organisation_id is >= 20000000 and NOMIS corporate_id is < 20000000.
 */
@Entity
@Table(name = "organisation")
data class OrganisationWithFixedIdEntity(
  @Id
  val organisationId: Long,
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
  override fun id(): Long = organisationId
}
