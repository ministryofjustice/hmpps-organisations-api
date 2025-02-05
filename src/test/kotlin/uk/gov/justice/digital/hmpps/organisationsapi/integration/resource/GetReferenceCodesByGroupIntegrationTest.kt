package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.organisationsapi.integration.SecureApiIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.integration.helper.hasSize
import uk.gov.justice.digital.hmpps.organisationsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.organisationsapi.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

class GetReferenceCodesByGroupIntegrationTest : SecureApiIntegrationTestBase() {
  @Autowired
  private lateinit var referenceCodeRepository: ReferenceCodeRepository

  override val allowedRoles: Set<String> = setOf("ROLE_ORGANISATIONS__R", "ROLE_ORGANISATIONS__RW")

  override fun baseRequestBuilder(): WebTestClient.RequestHeadersSpec<*> = webTestClient.get()
    .uri("/reference-codes/group/PHONE_TYPE")

  @Test
  fun `should return bad request if no matching code found`() {
    val error = webTestClient.get()
      .uri("/reference-codes/group/FOO")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_ORGANISATIONS__R")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    assertThat(error.developerMessage).startsWith(""""FOO" is not a valid reference code group. Valid groups are """)
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_ORGANISATIONS__R", "ROLE_ORGANISATIONS__RW"])
  fun `should return a list of relationship type reference codes`(role: String) {
    val groupCode = ReferenceCodeGroup.ADDRESS_TYPE
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted()) hasSize 3

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode, role = role)

    assertThat(listOfCodes).hasSize(3)
    assertThat(listOfCodes)
      .extracting("code")
      .containsAll(listOf("HOME", "BUS", "WORK"))
  }

  @Test
  fun `should return a list of phone type reference codes`() {
    val groupCode = ReferenceCodeGroup.PHONE_TYPE
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted()) hasSize 7

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode, role = "ROLE_ORGANISATIONS__R")

    assertThat(listOfCodes).extracting("code").containsExactlyInAnyOrder(
      "HOME",
      "BUS",
      "FAX",
      "ALTB",
      "ALTH",
      "MOB",
      "VISIT",
    )
  }

  @Test
  fun `should return a list of organisation address special needs type reference codes`() {
    val groupCode = ReferenceCodeGroup.ORG_ADDRESS_SPECIAL_NEEDS
    referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted()) hasSize 2

    val listOfCodes = testAPIClient.getReferenceCodes(groupCode, role = "ROLE_ORGANISATIONS__R")

    assertThat(listOfCodes).hasSize(2)
    assertThat(listOfCodes)
      .extracting("code")
      .containsAll(listOf("DEAF", "DISABLED"))
  }

  @Test
  fun `should be able to sort reference codes`() {
    val groupCode = ReferenceCodeGroup.TEST_SEQ_TYPE

    val listOfCodesInDisplayOrder = testAPIClient.getReferenceCodes(
      groupCode,
      "displayOrder",
      role = "ROLE_ORGANISATIONS__R",
    )

    assertThat(listOfCodesInDisplayOrder).extracting("code").isEqualTo(listOf("C", "B", "A"))

    val listOfCodesInCodeOrder = testAPIClient.getReferenceCodes(
      groupCode,
      "code",
      role = "ROLE_ORGANISATIONS__R",
    )

    assertThat(listOfCodesInCodeOrder).extracting("code").isEqualTo(listOf("A", "B", "C"))
  }

  @Test
  fun `should not return inactive codes by default`() {
    val groupCode = ReferenceCodeGroup.TEST_TYPE
    assertThat(testAPIClient.getReferenceCodes(groupCode, activeOnly = null, role = "ROLE_ORGANISATIONS__R"))
      .extracting("code")
      .isEqualTo(listOf("ACTIVE"))
  }

  @Test
  fun `should not return inactive codes if specifically request not to`() {
    val groupCode = ReferenceCodeGroup.TEST_TYPE
    assertThat(testAPIClient.getReferenceCodes(groupCode, activeOnly = true, role = "ROLE_ORGANISATIONS__R"))
      .extracting("code")
      .isEqualTo(listOf("ACTIVE"))
  }

  @Test
  fun `should return inactive codes if requested`() {
    val groupCode = ReferenceCodeGroup.TEST_TYPE
    assertThat(testAPIClient.getReferenceCodes(groupCode, activeOnly = false, role = "ROLE_ORGANISATIONS__R"))
      .extracting("code")
      .isEqualTo(listOf("ACTIVE", "INACTIVE"))
  }
}
