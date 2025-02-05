package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "organisation_type")
data class OrganisationTypeEntity(
  @EmbeddedId
  val id: OrganisationTypeId,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
