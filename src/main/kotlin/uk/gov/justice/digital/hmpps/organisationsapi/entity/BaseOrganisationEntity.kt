package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.LocalDate
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseOrganisationEntity(

  open val organisationName: String,

  open val programmeNumber: String?,

  open val vatNumber: String?,

  open val caseloadId: String?,

  open val comments: String?,

  open val active: Boolean,

  open val deactivatedDate: LocalDate?,

  @Column(updatable = false)
  open val createdBy: String,

  @Column(updatable = false)
  open val createdTime: LocalDateTime,

  open val updatedBy: String?,

  open val updatedTime: LocalDateTime?,
) {
  abstract fun id(): Long
}
