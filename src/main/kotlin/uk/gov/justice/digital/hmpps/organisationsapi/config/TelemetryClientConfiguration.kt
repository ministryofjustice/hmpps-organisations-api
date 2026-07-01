package uk.gov.justice.digital.hmpps.organisationsapi.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelemetryClientConfiguration {
  @Bean
  @ConditionalOnMissingBean(TelemetryClient::class)
  fun telemetryClient() = TelemetryClient()
}
