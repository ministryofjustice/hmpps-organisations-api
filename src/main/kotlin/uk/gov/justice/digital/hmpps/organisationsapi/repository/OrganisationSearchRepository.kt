package uk.gov.justice.digital.hmpps.organisationsapi.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationSummaryEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationSearchRequest

@Repository
class OrganisationSearchRepository(
  @PersistenceContext
  private var entityManager: EntityManager,
) {
  fun search(request: OrganisationSearchRequest, pageable: Pageable): Page<OrganisationSummaryEntity> {
    val cb = entityManager.criteriaBuilder
    val cq = cb.createQuery(OrganisationSummaryEntity::class.java)
    val entity = cq.from(OrganisationSummaryEntity::class.java)

    val predicates: List<Predicate> = buildPredicates(request, cb, entity)

    cq.where(*predicates.toTypedArray())

    applySorting(pageable, cq, cb, entity)

    val resultList = entityManager.createQuery(cq)
      .setFirstResult(pageable.offset.toInt())
      .setMaxResults(pageable.pageSize)
      .resultList

    val total = getTotalCount(request)

    return PageImpl(resultList, pageable, total)
  }

  private fun getTotalCount(
    request: OrganisationSearchRequest,
  ): Long {
    val cb = entityManager.criteriaBuilder
    val countQuery = cb.createQuery(Long::class.java)
    val entity = countQuery.from(OrganisationSummaryEntity::class.java)

    val predicates: List<Predicate> = buildPredicates(request, cb, entity)

    countQuery.select(cb.count(entity)).where(*predicates.toTypedArray<Predicate>())
    return entityManager.createQuery(countQuery).singleResult
  }

  private fun applySorting(
    pageable: Pageable,
    cq: CriteriaQuery<OrganisationSummaryEntity>,
    cb: CriteriaBuilder,
    entity: Root<OrganisationSummaryEntity>,
  ) {
    if (pageable.sort.isSorted) {
      pageable.sort.forEach {
        when {
          it.isAscending -> cq.orderBy(cb.asc(entity.get<String>(it.property)))
          else -> cq.orderBy(cb.desc(entity.get<String>(it.property)))
        }
      }
    }
  }

  private fun buildPredicates(
    request: OrganisationSearchRequest,
    cb: CriteriaBuilder,
    entity: Root<OrganisationSummaryEntity>,
  ): MutableList<Predicate> {
    val predicates: MutableList<Predicate> = ArrayList()
    predicates.add(cb.ilikePredicate(entity, "organisationName", request.name))
    return predicates
  }
}
