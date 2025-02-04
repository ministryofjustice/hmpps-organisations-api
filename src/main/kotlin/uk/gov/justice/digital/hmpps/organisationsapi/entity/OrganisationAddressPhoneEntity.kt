package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "organisation_address_phone")
data class OrganisationAddressPhoneEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val organisationAddressPhoneId: Long,

  val organisationId: Long,

  val organisationPhoneId: Long,

  val organisationAddressId: Long,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
