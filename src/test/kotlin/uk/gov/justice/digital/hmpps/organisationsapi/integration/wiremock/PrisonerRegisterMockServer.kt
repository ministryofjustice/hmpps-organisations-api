package uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.organisationsapi.client.prisonregister.PrisonName

class PrisonRegisterApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val prisonRegisterMockServer = PrisonRegisterMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    prisonRegisterMockServer.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    prisonRegisterMockServer.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    prisonRegisterMockServer.stop()
  }
}

class PrisonRegisterMockServer : MockServer(9995) {
  fun stubGetPrisonNamesWithId(prisonId: String, prison: PrisonName?) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/prisons/names"))
        .withQueryParam("prison_id", equalTo(prisonId))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(mapper.writeValueAsString(listOfNotNull(prison))),
        ),
    )
  }
}
