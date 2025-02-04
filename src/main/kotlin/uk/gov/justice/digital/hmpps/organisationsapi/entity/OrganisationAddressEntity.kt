package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "organisation_address")
data class OrganisationAddressEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val organisationAddressId: Long,

  val organisationId: Long,

  val addressType: String?,

  val primaryAddress: Boolean,

  val mailAddress: Boolean,

  val serviceAddress: Boolean,

  val noFixedAddress: Boolean,

  val flat: String?,

  val property: String?,

  val street: String?,

  val area: String?,

  val cityCode: String?,

  val countyCode: String?,

  val postCode: String?,

  val countryCode: String?,

  val specialNeedsCode: String?,

  val contactPersonName: String?,

  val businessHours: String?,

  val comments: String?,

  val startDate: LocalDate?,

  val endDate: LocalDate?,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
