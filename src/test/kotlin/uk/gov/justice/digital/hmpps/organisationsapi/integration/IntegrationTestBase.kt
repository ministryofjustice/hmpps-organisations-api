package uk.gov.justice.digital.hmpps.organisationsapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.organisationsapi.integration.containers.PostgresContainer
import uk.gov.justice.digital.hmpps.organisationsapi.integration.helper.TestAPIClient
import uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestConfiguration::class)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var stubEvents: StubOutboundEventsPublisher

  // @Autowired
  // private lateinit var organisationRepository: OrganisationWithFixedIdRepository

  protected lateinit var testAPIClient: TestAPIClient

  @BeforeEach
  fun setupTestApiClient() {
    testAPIClient = TestAPIClient(webTestClient, jwtAuthHelper)
    stubEvents.reset()
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  /*
  fun stubOrganisation(id: Long) {
    val entity = OrganisationWithFixedIdEntity(
      id,
      organisationName = "Name of $id",
      programmeNumber = null,
      vatNumber = null,
      caseloadId = null,
      comments = null,
      active = true,
      deactivatedDate = null,
      createdBy = "Created by",
      createdTime = LocalDateTime.now(),
      updatedBy = null,
      updatedTime = null,
    )
    organisationRepository.saveAndFlush(entity)
  }
   */

  companion object {
    private val pgContainer = PostgresContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.run {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl)
        registry.add("spring.datasource.username", pgContainer::getUsername)
        registry.add("spring.datasource.password", pgContainer::getPassword)
      }
    }
  }
}
