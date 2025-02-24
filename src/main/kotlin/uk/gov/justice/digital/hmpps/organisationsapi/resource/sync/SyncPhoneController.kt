package uk.gov.justice.digital.hmpps.organisationsapi.resource.sync

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.organisationsapi.facade.SyncFacade
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Migration and synchronisation")
@RestController
@RequestMapping(value = ["sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class SyncPhoneController(val syncFacade: SyncFacade) {

  @GetMapping(path = ["/organisation-phone/{organisationPhoneId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the data for one organisation phone number by ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to get the details for one organisation phone number.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returning the details of the organisation phone number",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncPhoneResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation phone number with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncGetPhoneById(
    @Parameter(description = "The internal ID for an organisation phone number.", required = true)
    @PathVariable organisationPhoneId: Long,
  ) = syncFacade.getPhoneById(organisationPhoneId)

  @DeleteMapping(path = ["/organisation-phone/{organisationPhoneId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Deletes one organisation organisation phone by internal ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to delete an organisation phone number.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted the organisation phone number",
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation phone number with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncDeletePhoneById(
    @Parameter(description = "The internal ID for the organisation phone number.", required = true)
    @PathVariable organisationPhoneId: Long,
  ) = syncFacade.deletePhone(organisationPhoneId)

  @PostMapping(path = ["/organisation-phone"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Creates a new phone number and links it to an organisation",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to create a new organisation phone number.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Successfully created the organisation phone number",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncPhoneResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request has invalid or missing fields",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncCreatePhone(
    @Valid @RequestBody syncCreatePhoneRequest: SyncCreatePhoneRequest,
  ) = syncFacade.createPhone(syncCreatePhoneRequest)

  @PutMapping(path = ["/organisation-phone/{organisationPhoneId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates an organisation phone number with altered or additional details",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to update an organisation's phone number.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated the organisation's phone number",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncPhoneResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The organisation phone number was not found",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncUpdatePhone(
    @Parameter(description = "The internal ID for the organisation phone number.", required = true)
    @PathVariable organisationPhoneId: Long,
    @Valid @RequestBody syncUpdatePhoneRequest: SyncUpdatePhoneRequest,
  ) = syncFacade.updatePhone(organisationPhoneId, syncUpdatePhoneRequest)
}
