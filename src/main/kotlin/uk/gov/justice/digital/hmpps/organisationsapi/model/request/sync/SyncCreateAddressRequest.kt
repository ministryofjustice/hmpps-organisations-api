package uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Request to create a new address for an organisation")
data class SyncCreateAddressRequest(

  @Schema(description = "The organisation ID linked with", example = "1233323")
  @field:NotNull(message = "The  organisation ID must be present in this request")
  val organisationId: Long,

  @Schema(description = "Type of address", example = "HOME")
  val addressType: String? = null,

  @Schema(description = "Primary address flag", example = "true")
  val primaryAddress: Boolean = false,

  @Schema(description = "Mail address flag", example = "true")
  val mailAddress: Boolean = false,

  @Schema(description = "Service address flag", example = "true")
  val serviceAddress: Boolean = false,

  @Schema(description = "No fixed address flag", example = "false")
  val noFixedAddress: Boolean = false,

  @Schema(description = "Flat number", example = "4A", nullable = true)
  val flat: String? = null,

  @Schema(description = "Property", example = "Claremont House", nullable = true)
  val property: String? = null,

  @Schema(description = "Street or road", example = "Clarendon Road", nullable = true)
  val street: String? = null,

  @Schema(description = "Area", example = "West Mosely", nullable = true)
  val area: String? = null,

  @Schema(description = "City code", example = "123456", nullable = true)
  val cityCode: String? = null,

  @Schema(description = "County code", example = "YORKS", nullable = true)
  val countyCode: String? = null,

  @Schema(description = "Postcode", example = "B25 1JH", nullable = true)
  val postcode: String? = null,

  @Schema(description = "Country code", example = "GB", nullable = true)
  val countryCode: String? = null,

  @Schema(description = "Special needs code", example = "NEEDS", nullable = true)
  val specialNeedsCode: String? = null,

  @Schema(description = "Contact name", example = "Mr Jones", nullable = true)
  val contactPersonName: String? = null,

  @Schema(description = "Business hours description", example = "9am - 5pm", nullable = true)
  val businessHours: String? = null,

  @Schema(description = "Notes on this address", example = "A comment", nullable = true)
  val comments: String? = null,

  @Schema(description = "The start date for occupation", example = "2024-01-01", nullable = true)
  val startDate: LocalDate? = null,

  @Schema(description = "The end date for occupation", example = "2024-01-01", nullable = true)
  val endDate: LocalDate? = null,

  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,

  @Schema(description = "The creation timestamp", example = "2024-01-01T00:00:00Z")
  val createdTime: LocalDateTime = LocalDateTime.now(),
)
