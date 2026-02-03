package uk.gov.justice.digital.hmpps.organisationsapi.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent

@Component
class FeatureSwitches(private val environment: Environment) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun isEnabled(feature: Feature, defaultValue: Boolean = false): Boolean = getBoolean(feature.label, defaultValue)

  fun isEnabled(outboundEvent: OutboundEvent, defaultValue: Boolean = false): Boolean = getBoolean("feature.event.${outboundEvent.eventType}", defaultValue)

  private fun getBoolean(property: String, defaultValue: Boolean): Boolean = environment.getProperty(property, Boolean::class.java, defaultValue).also {
    if (!environment.containsProperty(property)) {
      log.info("property '$property' not configured, defaulting to $defaultValue")
    }
  }
}

enum class Feature(val label: String) {
  OUTBOUND_EVENTS_ENABLED("feature.events.sns.enabled"),
}
