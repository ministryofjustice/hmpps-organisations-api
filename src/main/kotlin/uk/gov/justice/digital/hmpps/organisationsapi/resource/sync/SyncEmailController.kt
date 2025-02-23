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
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncEmailResponse
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Migration and synchronisation")
@RestController
@RequestMapping(value = ["sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class SyncEmailController(val syncFacade: SyncFacade) {

  @GetMapping(path = ["/organisation-email/{organisationEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the data for one organisation email address by ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to get the details for one organisation email address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returning the details of the organisation email address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncEmailResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation email address with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncGetEmailById(
    @Parameter(description = "The internal ID for an organisation email address.", required = true)
    @PathVariable organisationEmailId: Long,
  ) = syncFacade.getEmailById(organisationEmailId)

  @DeleteMapping(path = ["/organisation-email/{organisationEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Deletes one organisation organisation email address by internal ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to delete an organisation email address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted the organisation email address",
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation email address with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncDeleteEmailById(
    @Parameter(description = "The internal ID for the organisation email address.", required = true)
    @PathVariable organisationEmailId: Long,
  ) = syncFacade.deleteEmail(organisationEmailId)

  @PostMapping(path = ["/organisation-email"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Creates a new email address and links it to an organisation",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to create a new organisation email address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Successfully created the organisation email address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncEmailResponse::class),
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
  fun syncCreateEmail(
    @Valid @RequestBody syncCreateEmailRequest: SyncCreateEmailRequest,
  ) = syncFacade.createEmail(syncCreateEmailRequest)

  @PutMapping(path = ["/organisation-email/{organisationEmailId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates an organisation email address with altered or additional details",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to update an organisation's email address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated the organisation's email address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncEmailResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The organisation email address was not found",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncUpdateEmail(
    @Parameter(description = "The internal ID for the organisation email address.", required = true)
    @PathVariable organisationEmailId: Long,
    @Valid @RequestBody syncUpdateEmailRequest: SyncUpdateEmailRequest,
  ) = syncFacade.updateEmail(organisationEmailId, syncUpdateEmailRequest)
}
