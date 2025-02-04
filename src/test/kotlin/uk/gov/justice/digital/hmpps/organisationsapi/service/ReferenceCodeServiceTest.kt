package uk.gov.justice.digital.hmpps.organisationsapi.service

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.organisationsapi.entity.ReferenceCodeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.ReferenceCodeGroup
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.ReferenceCode
import uk.gov.justice.digital.hmpps.organisationsapi.repository.ReferenceCodeRepository

class ReferenceCodeServiceTest {
  private val referenceCodeRepository: ReferenceCodeRepository = mock()
  private val service = ReferenceCodeService(referenceCodeRepository)

  @BeforeEach
  fun setUp() {
    openMocks(this)
  }

  @Test
  fun `Should return a list of all references codes in a group`() {
    val groupCode = ReferenceCodeGroup.PHONE_TYPE
    val listOfCodes = listOf(
      ReferenceCodeEntity(1L, groupCode, "MOB", "Mobile", 0, false, "name"),
      ReferenceCodeEntity(2L, groupCode, "HOME", "Home", 1, true, "name"),
      ReferenceCodeEntity(3L, groupCode, "BUS", "Business", 2, true, "name"),
    )

    whenever(referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted())).thenReturn(listOfCodes)

    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted(), false)).isEqualTo(listOfCodes.toModel())

    verify(referenceCodeRepository).findAllByGroupCodeEquals(groupCode, Sort.unsorted())
  }

  @Test
  fun `Should return a list of only the active reference codes in a group`() {
    val groupCode = ReferenceCodeGroup.PHONE_TYPE
    val listOfCodes = listOf(
      ReferenceCodeEntity(2L, groupCode, "HOME", "Home", 1, true, "name"),
      ReferenceCodeEntity(3L, groupCode, "BUS", "Business", 2, true, "name"),
    )

    whenever(referenceCodeRepository.findAllByGroupCodeAndIsActiveEquals(groupCode, true, Sort.unsorted())).thenReturn(listOfCodes)

    val codes = service.getReferenceDataByGroup(groupCode, Sort.unsorted(), true)

    assertThat(codes).hasSize(2)
    assertThat(codes).extracting("code").containsExactly("HOME", "BUS")

    verify(referenceCodeRepository).findAllByGroupCodeAndIsActiveEquals(groupCode, true, Sort.unsorted())
  }

  @Test
  fun `Should return an empty list when no reference codes are in the group`() {
    val groupCode = ReferenceCodeGroup.PHONE_TYPE
    whenever(referenceCodeRepository.findAllByGroupCodeEquals(groupCode, Sort.unsorted())).thenReturn(emptyList())
    assertThat(service.getReferenceDataByGroup(groupCode, Sort.unsorted(), false)).isEmpty()
    verify(referenceCodeRepository).findAllByGroupCodeEquals(groupCode, Sort.unsorted())
  }

  @Test
  fun `should return a reference code if group and code are valid`() {
    val entity = ReferenceCodeEntity(1L, ReferenceCodeGroup.PHONE_TYPE, "MOBILE", "Mobile", 0, true, "name")
    whenever(referenceCodeRepository.findByGroupCodeAndCode(ReferenceCodeGroup.PHONE_TYPE, "MOBILE")).thenReturn(entity)

    val code = service.validateReferenceCode(ReferenceCodeGroup.PHONE_TYPE, "MOBILE", allowInactive = false)

    assertThat(code).isEqualTo(
      ReferenceCode(1L, ReferenceCodeGroup.PHONE_TYPE, "MOBILE", "Mobile", 0, true),
    )
  }

  @Test
  fun `should return reference code if inactive and inactive are allowed`() {
    val entity = ReferenceCodeEntity(1L, ReferenceCodeGroup.PHONE_TYPE, "MOBILE", "Mobile", 0, false, "name")
    whenever(referenceCodeRepository.findByGroupCodeAndCode(ReferenceCodeGroup.PHONE_TYPE, "MOBILE")).thenReturn(entity)

    val code = service.validateReferenceCode(ReferenceCodeGroup.PHONE_TYPE, "MOBILE", allowInactive = true)

    assertThat(code).isEqualTo(
      ReferenceCode(1L, ReferenceCodeGroup.PHONE_TYPE, "MOBILE", "Mobile", 0, false),
    )
  }

  @Test
  fun `should throw exception if reference code is inactive and is not allowed`() {
    val entity = ReferenceCodeEntity(1L, ReferenceCodeGroup.PHONE_TYPE, "BUS", "Business", 0, false, "name")
    whenever(referenceCodeRepository.findByGroupCodeAndCode(ReferenceCodeGroup.PHONE_TYPE, "BUS")).thenReturn(entity)

    val exception = assertThrows<ValidationException> {
      service.validateReferenceCode(ReferenceCodeGroup.PHONE_TYPE, "BUS", allowInactive = false)
    }

    assertThat(exception.message).isEqualTo("Unsupported phone type (BUS). This code is no longer active.")
  }

  @Test
  fun `should throw exception if reference code is not found`() {
    whenever(referenceCodeRepository.findByGroupCodeAndCode(ReferenceCodeGroup.PHONE_TYPE, "FOO")).thenReturn(null)

    val exception = assertThrows<ValidationException> {
      service.validateReferenceCode(ReferenceCodeGroup.PHONE_TYPE, "FOO", allowInactive = true)
    }

    assertThat(exception.message).isEqualTo("Unsupported phone type (FOO)")
  }
}
