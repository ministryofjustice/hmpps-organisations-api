package uk.gov.justice.digital.hmpps.organisationsapi.repository

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

fun <T> CriteriaBuilder.ilikePredicate(
  entity: Root<T>,
  field: String,
  value: String,
): Predicate = isTrue(
  function(
    "sql",
    Boolean::class.java,
    literal("? ILIKE ?"),
    entity.get<String>(field),
    literal("%${value.trim()}%"),
  ),
)
