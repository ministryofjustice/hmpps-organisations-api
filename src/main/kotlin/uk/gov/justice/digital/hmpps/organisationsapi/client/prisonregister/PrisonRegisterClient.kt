package uk.gov.justice.digital.hmpps.organisationsapi.client.prisonregister

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

interface PrisonRegisterClient {
  fun findPrisonNameById(id: String): PrisonName?
}

@Service
@Profile("!local")
class HmppsPrisonRegisterClient(
  @Qualifier("prisonerRegisterWebClient") private val webClient: WebClient,
) : PrisonRegisterClient {

  override fun findPrisonNameById(id: String): PrisonName? = webClient
    .get()
    .uri("/prisons/names?prison_id=$id")
    .retrieve()
    .bodyToMono<List<PrisonName>>()
    .block()!!
    .find { it.prisonId == id }
}

@Service
@Profile("local")
class LocalPrisonRegisterClient : PrisonRegisterClient {
  override fun findPrisonNameById(id: String): PrisonName? = null
}
