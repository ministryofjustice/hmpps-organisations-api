package uk.gov.justice.digital.hmpps.organisationsapi.integration.helper

import org.springframework.data.web.PagedModel
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.organisationsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressPhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationDetails
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate.MigrateOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncEmailResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationId
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncWebResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.net.URI

class TestAPIClient(private val webTestClient: WebTestClient, private val jwtAuthHelper: JwtAuthorisationHelper) {

  fun getOrganisation(id: Long, role: String = "ROLE_ORGANISATIONS__R"): OrganisationDetails = webTestClient.get()
    .uri("/organisation/$id")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(OrganisationDetails::class.java)
    .returnResult().responseBody!!

  fun getOrganisationSummary(id: Long, role: String = "ROLE_ORGANISATIONS__R"): OrganisationSummary = webTestClient.get()
    .uri("/organisation/$id/summary")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(OrganisationSummary::class.java)
    .returnResult().responseBody!!

  fun searchOrganisations(
    request: OrganisationSearchRequest,
    page: Long? = null,
    size: Long? = null,
    sort: List<String> = emptyList(),
    role: String = "ROLE_ORGANISATIONS__R",
  ): OrganisationSearchResponse = webTestClient.get()
    .uri("/organisation/search?name=${request.name}${page?.let {"&page=$page"} ?: "" }${size?.let {"&size=$size"} ?: "" }${sort.joinToString("") { "&sort=$it" }}")
    .accept(MediaType.APPLICATION_JSON)
    .headers(authorised(role))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(OrganisationSearchResponse::class.java)
    .returnResult().responseBody!!

  fun getReferenceCodes(
    groupCode: ReferenceCodeGroup,
    sort: String? = null,
    activeOnly: Boolean? = null,
    role: String = "ROLE_ORGANISATIONS__R",
  ): MutableList<ReferenceCode>? = webTestClient.get()
    .uri("/reference-codes/group/$groupCode?${sort?.let { "sort=$sort&" } ?: ""}${activeOnly?.let { "&activeOnly=$activeOnly" } ?: ""}")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBodyList(ReferenceCode::class.java)
    .returnResult().responseBody

  fun syncCreateAnOrganisation(request: SyncCreateOrganisationRequest) = webTestClient.post()
    .uri("/sync/organisation")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(SyncOrganisationResponse::class.java)
    .returnResult().responseBody!!

  fun syncCreateAnEmailAddress(request: SyncCreateEmailRequest) =
    webTestClient.post()
      .uri("/sync/organisation-email")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncEmailResponse::class.java)
      .returnResult().responseBody!!

  fun syncCreateAnAddress(request: SyncCreateAddressRequest) = webTestClient.post()
    .uri("/sync/organisation-address")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(SyncAddressResponse::class.java)
    .returnResult().responseBody!!

  fun syncCreateAnAddressPhone(request: SyncCreateAddressPhoneRequest) = webTestClient.post()
    .uri("/sync/organisation-address-phone")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(SyncAddressPhoneResponse::class.java)
    .returnResult().responseBody!!

  fun syncCreateAPhone(request: SyncCreatePhoneRequest) = webTestClient.post()
    .uri("/sync/organisation-phone")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(SyncPhoneResponse::class.java)
    .returnResult().responseBody!!

  fun syncCreateAWebAddress(request: SyncCreateWebRequest) =
    webTestClient.post()
      .uri("/sync/organisation-web")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
      .bodyValue(request)
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(SyncWebResponse::class.java)
      .returnResult().responseBody!!

  fun syncReconcileOrganisations(page: Long = 0, size: Long = 10) = webTestClient.get()
    .uri("/sync/organisations/reconcile?page=$page&size=$size")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS_MIGRATION")))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(OrganisationIdsResponse::class.java)
    .returnResult().responseBody!!

  fun getBadResponseErrors(uri: URI) = webTestClient.get()
    .uri(uri.toString())
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__R")))
    .exchange()
    .expectStatus()
    .isBadRequest
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(ErrorResponse::class.java)
    .returnResult().responseBody!!

  fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  fun migrateAnOrganisation(
    request: MigrateOrganisationRequest,
    authRole: String = "ROLE_ORGANISATIONS_MIGRATION",
  ) = webTestClient.post()
    .uri("/migrate/organisation")
    .accept(MediaType.APPLICATION_JSON)
    .contentType(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(authRole)))
    .bodyValue(request)
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(MigrateOrganisationResponse::class.java)
    .returnResult().responseBody!!

  private fun authorised(role: String = "ROLE_ORGANISATIONS__RW") = setAuthorisation(roles = listOf(role))

  data class OrganisationSearchResponse(
    val content: List<OrganisationSummary>,
    val page: PagedModel.PageMetadata,
  )

  data class OrganisationIdsResponse(
    val content: List<SyncOrganisationId>,
    val page: PagedModel.PageMetadata,
  )
}
