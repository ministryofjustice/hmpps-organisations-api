package uk.gov.justice.digital.hmpps.organisationsapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.organisationsapi.client.prisonregister.PrisonName
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.integration.helper.TestAPIClient
import uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock.PrisonRegisterApiExtension
import uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock.PrisonRegisterApiExtension.Companion.prisonRegisterMockServer
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.LocalDateTime

@ExtendWith(HmppsAuthApiExtension::class, PrisonRegisterApiExtension::class)
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

  @Autowired
  private lateinit var organisationRepository: OrganisationWithFixedIdRepository

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

  fun stubPrisonRegisterGetNamesById(id: String, name: String) {
    prisonRegisterMockServer.stubGetPrisonNamesWithId(prisonId = id, PrisonName(prisonId = id, prisonName = name))
  }

  fun stubPrisonRegisterGetNamesByIdNotFound(id: String) {
    prisonRegisterMockServer.stubGetPrisonNamesWithId(prisonId = id, null)
  }
}
