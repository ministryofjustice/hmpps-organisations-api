package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource

import org.apache.commons.lang3.RandomUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.organisationsapi.integration.SecureApiIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.integration.helper.TestAPIClient.OrganisationSearchResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationRequest

class OrganisationV2ListIntegrationTest : SecureApiIntegrationTestBase() {

  override val allowedRoles: Set<String> = setOf("ROLE_ORGANISATIONS__R", "ROLE_ORGANISATIONS__RW")

  override fun baseRequestBuilder(): WebTestClient.RequestHeadersSpec<*> = webTestClient.get()
    .uri("/organisation/v2")
    .accept(MediaType.APPLICATION_JSON)

  @Test
  fun `should list all organisations in name order when no name is given`() {
    createOrg("List Zebra Ltd")
    createOrg("List Aardvark Ltd")

    val response = list("/organisation/v2?size=1000")

    val names = response.content.map { it.organisationName }
    assertThat(names).contains("List Zebra Ltd", "List Aardvark Ltd")
    assertThat(names).isSorted()
    assertThat(response.page.totalElements).isGreaterThanOrEqualTo(2)
  }

  @Test
  fun `should filter by partial name and page the results`() {
    createOrg("Pagey Org One")
    createOrg("Pagey Org Two")
    createOrg("Pagey Org Three")

    val response = list("/organisation/v2?name=pagey&size=2&page=1")

    assertThat(response.page.totalElements).isEqualTo(3)
    assertThat(response.content).hasSize(1)
    assertThat(response.content.single().organisationName).isEqualTo("Pagey Org Two")
  }

  private fun list(uri: String): OrganisationSearchResponse = webTestClient.get()
    .uri(uri)
    .accept(MediaType.APPLICATION_JSON)
    .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__R")))
    .exchange()
    .expectStatus().isOk
    .expectBody(OrganisationSearchResponse::class.java)
    .returnResult().responseBody!!

  private fun createOrg(name: String) {
    testAPIClient.migrateAnOrganisation(
      MigrateOrganisationRequest(
        nomisCorporateId = RandomUtils.secure().randomLong(100000, 999999),
        organisationName = name,
        programmeNumber = null,
        vatNumber = null,
        caseloadId = null,
        comments = null,
        active = true,
        deactivatedDate = null,
        organisationTypes = emptyList(),
        phoneNumbers = emptyList(),
        emailAddresses = emptyList(),
        webAddresses = emptyList(),
        addresses = emptyList(),
      ),
    )
  }
}
