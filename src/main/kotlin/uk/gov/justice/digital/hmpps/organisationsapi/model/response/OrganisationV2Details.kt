package uk.gov.justice.digital.hmpps.organisationsapi.model.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2AddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2CreateRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2InternetAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2PhoneRequest
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Created organisation with all supplied legacy NOMIS data")
data class OrganisationV2Details(
  @Schema(description = "Generated organisation ID", example = "20000000")
  val organisationId: Long,
  val organisationName: String,
  val programmeNumber: String?,
  val vatNumber: String?,
  val caseloadId: String?,
  val comments: String?,
  val organisationTypes: List<String>,
  val active: Boolean,
  val deactivatedDate: LocalDate?,
  val phoneNumbers: List<OrganisationV2PhoneRequest>,
  val internetAddresses: List<OrganisationV2InternetAddressRequest>,
  val addresses: List<OrganisationV2AddressRequest>,
  val createdBy: String,
  val createdTime: LocalDateTime,
  val updatedBy: String?,
  val updatedTime: LocalDateTime?,
) {
  companion object {
    fun from(organisationId: Long, request: OrganisationV2CreateRequest) = OrganisationV2Details(
      organisationId = organisationId,
      organisationName = request.organisationName,
      programmeNumber = request.programmeNumber,
      vatNumber = request.vatNumber,
      caseloadId = request.caseloadId,
      comments = request.comments,
      organisationTypes = request.organisationTypes,
      active = request.active,
      deactivatedDate = request.deactivatedDate,
      phoneNumbers = request.phoneNumbers,
      internetAddresses = request.internetAddresses,
      addresses = request.addresses,
      createdBy = request.createdBy,
      createdTime = request.createdTime,
      updatedBy = request.updatedBy,
      updatedTime = request.updatedTime,
    )
  }
}
