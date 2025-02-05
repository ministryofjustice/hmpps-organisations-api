package uk.gov.justice.digital.hmpps.organisationsapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "v_organisation_phone_numbers")
data class OrganisationPhoneDetailsEntity(
  @Id
  val organisationPhoneId: Long,

  val organisationId: Long,

  val phoneType: String,

  val phoneTypeDescription: String,

  val phoneNumber: String,

  val extNumber: String?,

  val createdBy: String,

  val createdTime: LocalDateTime,

  val updatedBy: String?,

  val updatedTime: LocalDateTime?,
)
