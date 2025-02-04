package uk.gov.justice.digital.hmpps.organisationsapi.repository

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.organisationsapi.entity.ReferenceCodeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.ReferenceCodeGroup

@Repository
interface ReferenceCodeRepository : JpaRepository<ReferenceCodeEntity, Long> {
  fun findAllByGroupCodeEquals(groupCode: ReferenceCodeGroup, sort: Sort): List<ReferenceCodeEntity>
  fun findAllByGroupCodeAndIsActiveEquals(groupCode: ReferenceCodeGroup, isActive: Boolean, sort: Sort): List<ReferenceCodeEntity>
  fun findByGroupCodeAndCode(groupCode: ReferenceCodeGroup, code: String): ReferenceCodeEntity?
}
