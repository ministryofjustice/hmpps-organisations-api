package uk.gov.justice.digital.hmpps.organisationsapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class HmppsAuthApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val hmppsAuthMockServer = HmppsAuthMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    hmppsAuthMockServer.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    hmppsAuthMockServer.resetRequests()
    hmppsAuthMockServer.stubGrantToken()
  }

  override fun afterAll(context: ExtensionContext) {
    hmppsAuthMockServer.stop()
  }
}

class HmppsAuthMockServer : MockServer(WIREMOCK_PORT, "/auth") {
  companion object {
    private const val WIREMOCK_PORT = 8090
  }

  fun stubGrantToken() {
    stubFor(
      post(urlEqualTo("/auth/oauth/token"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                  "token_type": "bearer",
                  "access_token": "ABCDE"
                }
              """.trimIndent(),
            ),
        ),
    )
  }
}
