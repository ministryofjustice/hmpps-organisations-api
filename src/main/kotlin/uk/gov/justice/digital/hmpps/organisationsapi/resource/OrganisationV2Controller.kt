package uk.gov.justice.digital.hmpps.organisationsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.organisationsapi.facade.OrganisationFacade
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationV2CreateRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationV2Details
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Organisations")
@RestController
@RequestMapping(value = ["organisation/v2"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class OrganisationV2Controller(private val organisationFacade: OrganisationFacade) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Create new organisation with full details",
    description = "Creates an organisation and all supplied types, addresses and contact details.",
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Created the organisation successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrganisationV2Details::class),
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
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS__RW')")
  fun createOrganisation(
    @Valid @RequestBody request: OrganisationV2CreateRequest,
  ): ResponseEntity<OrganisationV2Details> = organisationFacade.createV2(request)
    .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

  @PutMapping("/{organisationId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Replace an organisation with full details",
    description = "Replaces the organisation and all of its types, addresses and contact details with those supplied. Omitted child records are removed.",
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Updated the organisation successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrganisationV2Details::class),
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
        description = "The organisation was not found",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS__RW')")
  fun updateOrganisation(
    @PathVariable organisationId: Long,
    @Valid @RequestBody request: OrganisationV2CreateRequest,
  ): OrganisationV2Details = organisationFacade.updateV2(organisationId, request)
}
