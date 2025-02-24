package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "organisation_email")
data class OrganisationEmailEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val organisationEmailId: Long,

  val organisationId: Long,

  val emailAddress: String,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  val createdTime: LocalDateTime,

  val updatedBy: String? = null,

  val updatedTime: LocalDateTime? = null,
)
