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
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncWebResponse
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Migration and synchronisation")
@RestController
@RequestMapping(value = ["sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class SyncWebController(val syncFacade: SyncFacade) {

  @GetMapping(path = ["/organisation-web/{organisationWebId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the data for one organisation web address by ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to get the details for one organisation web address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returning the details of the organisation web address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncWebResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation web address with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncGetWebById(
    @Parameter(description = "The internal ID for an organisation web address.", required = true)
    @PathVariable organisationWebId: Long,
  ) = syncFacade.getWebById(organisationWebId)

  @DeleteMapping(path = ["/organisation-web/{organisationWebId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Deletes one organisation organisation web address by internal ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to delete an organisation web address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted the organisation web address",
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation web address with the requested ID was found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncDeleteWebById(
    @Parameter(description = "The internal ID for the organisation web address.", required = true)
    @PathVariable organisationWebId: Long,
  ) = syncFacade.deleteWeb(organisationWebId)

  @PostMapping(path = ["/organisation-web"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Creates a new web address and links it to an organisation",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to create a new organisation web address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Successfully created the organisation web address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncWebResponse::class),
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
  fun syncCreateWeb(
    @Valid @RequestBody syncCreateWebRequest: SyncCreateWebRequest,
  ) = syncFacade.createWeb(syncCreateWebRequest)

  @PutMapping(path = ["/organisation-web/{organisationWebId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates an organisation web address with altered or additional details",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to update an organisation's web address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated the organisation's web address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncWebResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The organisation web address was not found",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request data",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncUpdateWeb(
    @Parameter(description = "The internal ID for the organisation web address.", required = true)
    @PathVariable organisationWebId: Long,
    @Valid @RequestBody syncUpdateWebRequest: SyncUpdateWebRequest,
  ) = syncFacade.updateWeb(organisationWebId, syncUpdateWebRequest)
}
