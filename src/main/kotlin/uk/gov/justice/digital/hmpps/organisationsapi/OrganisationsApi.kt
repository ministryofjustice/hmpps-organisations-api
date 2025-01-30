package uk.gov.justice.digital.hmpps.organisationsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrganisationsApi

fun main(args: Array<String>) {
  runApplication<OrganisationsApi>(*args)
}
