package uk.gov.justice.digital.hmpps.organisationsapi.repository

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationSummaryEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary
import kotlin.reflect.full.memberProperties

class MapDomainToEntityForSortKtTest {

  @Test
  fun `can map contact search result item fields to contact with address entity fields`() {
    OrganisationSummary::class.memberProperties.forEach { property ->
      val mapped = mapSortPropertiesOfOrgSearch(property.name)
      assertThat(mapped).isNotNull()
      assertThat(OrganisationSummaryEntity::class.memberProperties.find { it.name == mapped }).isNotNull()
    }
  }

  @Test
  fun `attempting to sort on an invalid field for contact search gives an error`() {
    val expected = org.junit.jupiter.api.assertThrows<ValidationException> {
      mapSortPropertiesOfOrgSearch("foo")
    }
    assertThat(expected.message).isEqualTo("Unable to sort on foo")
  }
}
