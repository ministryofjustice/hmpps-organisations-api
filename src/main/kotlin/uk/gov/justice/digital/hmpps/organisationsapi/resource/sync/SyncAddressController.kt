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
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressResponse
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Migration and synchronisation")
@RestController
@RequestMapping(value = ["sync"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class SyncAddressController(val syncFacade: SyncFacade) {

  @GetMapping(path = ["/organisation-address/{organisationAddressId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Returns the data for one organisation address by ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to get the details for one organisation address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returning the details of the organisation address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncAddressResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request has invalid or missing fields",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation address with the requested ID was found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncGetAddressById(
    @Parameter(description = "The internal ID for an organisation address", required = true)
    @PathVariable organisationAddressId: Long,
  ) = syncFacade.getAddressById(organisationAddressId)

  @DeleteMapping(path = ["/organisation-address/{organisationAddressId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Deletes one organisation organisation address by internal ID",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to delete an organisation address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Successfully deleted the organisation address",
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation address with the requested ID was found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun syncDeleteAddressById(
    @Parameter(description = "The internal ID for the organisation address.", required = true)
    @PathVariable organisationAddressId: Long,
  ) = syncFacade.deleteAddress(organisationAddressId)

  @PostMapping(path = ["/organisation-address"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Creates a new address and links it to an organisation",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to create a new organisation address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Successfully created the organisation address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncAddressResponse::class),
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
  fun syncCreateAddress(
    @Valid @RequestBody syncCreateAddressRequest: SyncCreateAddressRequest,
  ) = syncFacade.createAddress(syncCreateAddressRequest)

  @PutMapping(path = ["/organisation-address/{organisationAddressId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Updates an organisation address with altered or additional details",
    description = """
      Requires role: ROLE_ORGANISATIONS_MIGRATION.
      Used to update an organisation's address.
      """,
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Successfully updated the organisation's address",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SyncAddressResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The organisation address was not found",
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
  fun syncUpdateAddress(
    @Parameter(description = "The internal ID for the organisation address.", required = true)
    @PathVariable organisationAddressId: Long,
    @Valid @RequestBody syncUpdateAddressRequest: SyncUpdateAddressRequest,
  ) = syncFacade.updateAddress(organisationAddressId, syncUpdateAddressRequest)
}
