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
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Migration and synchronisation")
@RestController
@RequestMapping(value = ["sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class SyncOrganisationController(val syncFacade: SyncFacade) {

  @GetMapping(path = ["/organisation/{organisationId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the data for one organisation by organisationId",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to get the details for one organisation.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returning the details of the organisation",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncOrganisationResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncGetOrganisationById(
    @Parameter(description = "The internal ID for an organisation.", required = true)
    @PathVariable organisationId: Long,
  ) = syncFacade.getOrganisationById(organisationId)

  @DeleteMapping(path = ["/organisation/{organisationId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Deletes one organisation by internal ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to delete an organisation.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted the organisation",
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncDeleteOrganisationById(
    @Parameter(description = "The internal ID for the organisation.", required = true)
    @PathVariable organisationId: Long,
  ) = syncFacade.deleteOrganisation(organisationId)

  @PostMapping(path = ["/organisation"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Creates a new organisation with a specified ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to create a new organisation.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Successfully created the organisation",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncOrganisationResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request has invalid or missing fields",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "409",
        description = "Conflict. The organisation ID provided in the request already exists",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncCreateOrganisation(
    @Valid @RequestBody createOrganisationRequest: SyncCreateOrganisationRequest,
  ) = syncFacade.createOrganisation(createOrganisationRequest)

  @PutMapping(path = ["/organisation/{organisationId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates an organisation with altereed or additional details",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to update an organisation.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated the organisation",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncOrganisationResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The organisation was not found",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncUpdateOrganisation(
    @Parameter(description = "The internal ID for the organisation.", required = true)
    @PathVariable organisationId: Long,
    @Valid @RequestBody updateOrganisationRequest: SyncUpdateOrganisationRequest,
  ) = syncFacade.updateOrganisation(organisationId, updateOrganisationRequest)
}
