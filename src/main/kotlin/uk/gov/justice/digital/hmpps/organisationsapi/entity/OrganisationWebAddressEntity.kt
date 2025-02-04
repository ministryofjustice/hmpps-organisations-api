package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "organisation_web_address")
data class OrganisationWebAddressEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val organisationWebAddressId: Long,

  val organisationId: Long,

  val webAddress: String,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
