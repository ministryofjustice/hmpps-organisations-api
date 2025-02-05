package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "v_organisation_types")
data class OrganisationTypeDetailsEntity(
  @EmbeddedId
  val id: OrganisationTypeId,

  val organisationTypeDescription: String,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
