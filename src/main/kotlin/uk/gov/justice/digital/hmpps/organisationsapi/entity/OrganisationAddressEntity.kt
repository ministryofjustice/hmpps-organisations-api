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

  val addressType: String? = null,

  val primaryAddress: Boolean,

  val mailAddress: Boolean,

  val serviceAddress: Boolean,

  val noFixedAddress: Boolean,

  val flat: String? = null,

  val property: String? = null,

  val street: String? = null,

  val area: String? = null,

  val cityCode: String? = null,

  val countyCode: String? = null,

  val postCode: String? = null,

  val countryCode: String? = null,

  val specialNeedsCode: String? = null,

  val contactPersonName: String? = null,

  val businessHours: String? = null,

  val comments: String? = null,

  val startDate: LocalDate? = null,

  val endDate: LocalDate? = null,

  @Column(updatable = false)
  val createdBy: String,

  @Column(updatable = false)
  val createdTime: LocalDateTime,

  val updatedBy: String? = null,

  val updatedTime: LocalDateTime? = null,
)
