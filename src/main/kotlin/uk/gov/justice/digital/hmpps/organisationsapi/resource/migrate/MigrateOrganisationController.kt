package uk.gov.justice.digital.hmpps.organisationsapi.resource.migrate

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate.MigrateOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.migrate.MigrateOrganisationService
import uk.gov.justice.digital.hmpps.organisationsapi.swagger.AuthApiResponses
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Migration and synchronisation")
@RestController
@RequestMapping(value = ["migrate/organisation"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class MigrateOrganisationController(val migrationService: MigrateOrganisationService) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Migrate an organisation",
    description = "Migrate an organisation from NOMIS with all of its associated data. If an organisation with the same id id already exists it will be replaced",
    security = [SecurityRequirement(name = "bearer")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "The organisation and associated data was created successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = MigrateOrganisationResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request failed validation with invalid or missing data supplied",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ORGANISATIONS_MIGRATION')")
  fun migrateOrganisation(
    @Valid @RequestBody request: MigrateOrganisationRequest,
  ): MigrateOrganisationResponse = migrationService.migrateOrganisation(request)
}
