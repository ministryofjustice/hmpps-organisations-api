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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.organisationsapi.facade.SyncFacade
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateTypesRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncTypesResponse
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Migration and synchronisation")
@RestController
@RequestMapping(value = ["sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class SyncTypesController(val syncFacade: SyncFacade) {

  @GetMapping(path = ["/organisation-types/{organisationId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the organisation types for an organisation by ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to get the organisation types for this organisation.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returning the details of the organisation types",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncTypesResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation with the requested ID was found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncGetTypesByOrganisationId(
    @Parameter(description = "The internal organisation ID.", required = true)
    @PathVariable organisationId: Long,
  ) = syncFacade.getTypesByOrganisationId(organisationId)

  @PutMapping(path = ["/organisation-types/{organisationId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates the organisation types for a given organisation ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to update an organisation's types.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated the organisation's types",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncTypesResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The organisation ID was not found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncUpdateWeb(
    @Parameter(description = "The internal organisation ID.", required = true)
    @PathVariable organisationId: Long,
    @Valid @RequestBody syncUpdateTypesRequest: SyncUpdateTypesRequest,
  ) = syncFacade.updateTypes(organisationId, syncUpdateTypesRequest)
}
