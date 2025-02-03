package uk.gov.justice.digital.hmpps.organisationsapi.integration.helper

import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

class TestAPIClient(private val webTestClient: WebTestClient, private val jwtAuthHelper: JwtAuthorisationHelper) {

  /*
  fun getOrganisation(id: Long, role: String = "ROLE_CONTACTS_ADMIN"): OrganisationDetails = webTestClient.get()
    .uri("/organisation/$id")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
    .exchange()
    .expectStatus()
    .isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(OrganisationDetails::class.java)
    .returnResult().responseBody!!

  fun searchOrganisations(request: OrganisationSearchRequest, page: Long? = null, size: Long? = null, sort: List<String> = emptyList(), role: String = "ROLE_CONTACTS_ADMIN"): OrganisationSearchResponse = webTestClient.get()
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
    role: String = "ROLE_CONTACTS_ADMIN",
  ): MutableList<ReferenceCode>? = webTestClient.get()
    .uri("/reference-codes/group/$groupCode?${sort?.let { "sort=$sort&" } ?: ""}${activeOnly?.let { "&activeOnly=$activeOnly" } ?: ""}")
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf(role)))
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBodyList(ReferenceCode::class.java)
    .returnResult().responseBody

  fun getBadResponseErrors(uri: URI) = webTestClient.get()
    .uri(uri.toString())
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_CONTACTS_ADMIN")))
    .exchange()
    .expectStatus()
    .isBadRequest
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(ErrorResponse::class.java)
    .returnResult().responseBody!!
   */

  fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  /*
  fun migrateAnOrganisation(request: MigrateOrganisationRequest, authRole: String = "ROLE_CONTACTS_MIGRATION") = webTestClient.post()
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
   */

  private fun authorised(role: String = "ROLE_ORGANISATIONS__RW") = setAuthorisation(roles = listOf(role))

  /*
  data class OrganisationSearchResponse(
    val content: List<OrganisationSummary>,
    val pageable: ReturnedPageable,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val sort: ReturnedSort,
    val numberOfElements: Int,
    val empty: Boolean,
  )
   */

  data class ReturnedPageable(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: ReturnedSort,
    val offset: Int,
    val unpaged: Boolean,
    val paged: Boolean,
  )

  data class ReturnedSort(
    val empty: Boolean,
    val unsorted: Boolean,
    val sorted: Boolean,
  )
}
