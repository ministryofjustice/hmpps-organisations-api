package uk.gov.justice.digital.hmpps.organisationsapi.client.prisonregister

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class PrisonRegisterClient(
  @Qualifier("prisonerRegisterWebClient") private val webClient: WebClient,
) {

  fun findPrisonNameById(id: String): PrisonName? =
    webClient
      .get()
      .uri("/prisons/names?prison_id=$id")
      .retrieve()
      .bodyToMono<List<PrisonName>>()
      .block()!!
      .find { it.prisonId == id }
}
