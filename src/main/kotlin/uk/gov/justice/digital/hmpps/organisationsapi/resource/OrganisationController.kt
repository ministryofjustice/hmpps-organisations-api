package uk.gov.justice.digital.hmpps.organisationsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.organisationsapi.facade.OrganisationFacade
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummaryResultItemPage
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Organisations")
@RestController
@RequestMapping(value = ["organisation"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class OrganisationController(private val organisationFacade: OrganisationFacade) {

  @GetMapping("/{organisationId}")
  @Operation(
    summary = "Get organisation",
    description = "Gets a organisation by their id",
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the organisation",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrganisationDetails::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS__R', 'ROLE_ORGANISATIONS__RW')")
  fun getOrganisationById(@PathVariable organisationId: Long): OrganisationDetails = organisationFacade.getOrganisationById(organisationId)

  @GetMapping("/{organisationId}/summary")
  @Operation(
    summary = "Get organisation summary",
    description = "Gets a summary of an organisation by their id. Includes primary address and any business phone number for that address.",
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Found the organisation",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrganisationSummary::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No organisation with that id could be found",
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS__R', 'ROLE_ORGANISATIONS__RW')")
  fun getOrganisationSummaryById(@PathVariable organisationId: Long): OrganisationSummary = organisationFacade.getOrganisationSummaryById(organisationId)

  @Deprecated(message = "This endpoint is deprecated and should not be used to create organisations. Changes made will not be synchronised to NOMIS.")
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Create new organisation",
    description = "Creates a new organisation in DPS but does not currently synchronise to NOMIS. Deprecated until this 2-way sync is in place.",
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
            schema = Schema(implementation = OrganisationDetails::class),
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
    @Valid @RequestBody request: CreateOrganisationRequest,
  ): ResponseEntity<OrganisationDetails> = organisationFacade.create(request)
    .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

  @GetMapping("/search")
  @Operation(
    summary = "Search organisations",
    description = "Search all organisations by their name",
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Organisations searched successfully. There may be no results.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrganisationSummaryResultItemPage::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS__R', 'ROLE_ORGANISATIONS__RW')")
  fun searchOrganisations(
    @ModelAttribute @Valid @Parameter(
      description = "Search criteria",
      required = true,
    ) request: OrganisationSearchRequest,
    @Parameter(description = "Pageable configurations", required = false)
    @PageableDefault(sort = ["organisationName"], direction = Direction.ASC)
    pageable: Pageable,
  ) = organisationFacade.search(request, pageable)
}
