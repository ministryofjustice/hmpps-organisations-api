package uk.gov.justice.digital.hmpps.organisationsapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.hmpps-auth}") val hmppsAuthBaseUri: String,
  @Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @Value("\${api.timeout:30s}") val timeout: Duration,
  private val builder: WebClient.Builder,
) {
  @Bean
  fun hmppsAuthHealthWebClient(): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)
}
