package uk.gov.justice.digital.hmpps.organisationsapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationSummaryEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.CreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.OrganisationSearchRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.OrganisationSummary
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationEmailRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationSearchRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationSummaryRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeDetailsRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWebAddressRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class OrganisationServiceTest {
  private val organisationRepository: OrganisationRepository = mock()
  private val organisationSearchRepository: OrganisationSearchRepository = mock()
  private val organisationPhoneDetailsRepository: OrganisationPhoneDetailsRepository = mock()
  private val organisationAddressPhoneRepository: OrganisationAddressPhoneRepository = mock()
  private val organisationTypeDetailsRepository: OrganisationTypeDetailsRepository = mock()
  private val organisationEmailRepository: OrganisationEmailRepository = mock()
  private val organisationWebAddressRepository: OrganisationWebAddressRepository = mock()
  private val organisationAddressRepository: OrganisationAddressDetailsRepository = mock()
  private val organisationSummaryRepository: OrganisationSummaryRepository = mock()

  private val organisationService: OrganisationService = OrganisationService(
    organisationRepository,
    organisationSearchRepository,
    organisationPhoneDetailsRepository,
    organisationAddressPhoneRepository,
    organisationTypeDetailsRepository,
    organisationEmailRepository,
    organisationWebAddressRepository,
    organisationAddressRepository,
    organisationSummaryRepository,
  )

  @Nested
  inner class GetOrganisationByOrganisationId {

    @Test
    fun `should return a organisation when valid id is provided`() {
      // Given
      val orgId = 1L
      val savedEntity = createOrganisationEntity(
        deactivatedDate = LocalDate.now(),
        createdTime = LocalDateTime.now().minusMinutes(20),
        updatedTime = LocalDateTime.now().plusMinutes(20),
      )
      whenever(organisationRepository.findById(orgId)).thenReturn(Optional.of(savedEntity))

      // When
      val result = organisationService.getOrganisationById(orgId)

      // Then
      assertNotNull(result)
      with(result) {
        assertThat(organisationId).isEqualTo(orgId)
        assertThat(organisationName).isEqualTo(savedEntity.organisationName)
        assertThat(programmeNumber).isEqualTo(savedEntity.programmeNumber)
        assertThat(vatNumber).isEqualTo(savedEntity.vatNumber)
        assertThat(caseloadId).isEqualTo(savedEntity.caseloadId)
        assertThat(comments).isEqualTo(savedEntity.comments)
        assertThat(active).isEqualTo(savedEntity.active)
        assertThat(deactivatedDate).isEqualTo(savedEntity.deactivatedDate)
        assertThat(createdBy).isEqualTo(savedEntity.createdBy)
        assertThat(createdTime).isEqualTo(savedEntity.createdTime)
        assertThat(updatedBy).isEqualTo(savedEntity.updatedBy)
        assertThat(updatedTime).isEqualTo(savedEntity.updatedTime)
      }
    }

    @Test
    fun `should return error when organisation id does not exist`() {
      // Given
      val organisationId = 1009L
      whenever(organisationRepository.findById(organisationId)).thenReturn(Optional.empty())

      // When
      val exception = assertThrows<EntityNotFoundException> {
        organisationService.getOrganisationById(organisationId)
      }

      // Then
      assertThat(exception.message).isEqualTo("Organisation with id 1009 not found")
    }
  }

  @Nested
  inner class OrganisationCreate {

    @Test
    fun `create successfully creates new organisation`() {
      // Given
      val request = CreateOrganisationRequest(
        organisationName = "Name",
        programmeNumber = "P1",
        vatNumber = "V1",
        caseloadId = "C1",
        active = false,
        deactivatedDate = LocalDate.now(),
        createdBy = "Created by",
        createdTime = LocalDateTime.now().minusMinutes(20),
        updatedBy = "U1",
        updatedTime = LocalDateTime.now().plusMinutes(20),
        comments = "C2",
      )

      val savedEntity = createOrganisationEntity(
        deactivatedDate = request.deactivatedDate,
        createdTime = request.createdTime,
        updatedTime = request.updatedTime,
      )

      whenever(organisationRepository.saveAndFlush(any())).thenReturn(savedEntity)

      // When
      val result = organisationService.create(request)

      // Then
      assertNotNull(result)
      with(result) {
        assertThat(organisationId).isEqualTo(1L)
        assertThat(organisationName).isEqualTo(request.organisationName)
        assertThat(programmeNumber).isEqualTo(request.programmeNumber)
        assertThat(vatNumber).isEqualTo(request.vatNumber)
        assertThat(caseloadId).isEqualTo(request.caseloadId)
        assertThat(comments).isEqualTo(request.comments)
        assertThat(active).isFalse()
        assertThat(deactivatedDate).isEqualTo(request.deactivatedDate)
        assertThat(createdBy).isEqualTo(request.createdBy)
        assertThat(createdTime).isEqualTo(request.createdTime)
        assertThat(updatedBy).isEqualTo(request.updatedBy)
        assertThat(updatedTime).isEqualTo(request.updatedTime)
      }

      verify(organisationRepository).saveAndFlush(any())
    }
  }

  @Nested
  inner class SearchOrganisations {

    private val request = OrganisationSearchRequest("Some name")
    private val pageable = Pageable.ofSize(99)

    @Test
    fun `should return search results`() {
      // Given
      val resultEntity = OrganisationSummaryEntity(
        organisationId = 123,
        organisationName = "Some name limited",
        organisationActive = true,
        flat = "Flat",
        property = "Property",
        street = "Street",
        area = "Area",
        cityCode = "123",
        cityDescription = "City",
        countyCode = "C.OUNTY",
        countyDescription = "County",
        postCode = "AB12 3CD",
        countryCode = "COU",
        countryDescription = "Country",
        businessPhoneNumber = "0123456",
        businessPhoneNumberExtension = "789",
      )
      whenever(organisationSearchRepository.search(request, pageable)).thenReturn(
        PageImpl(
          listOf(resultEntity),
          pageable,
          1,
        ),
      )

      // When
      val result = organisationService.search(request, pageable)

      // Then
      assertThat(result.content).hasSize(1)
      assertThat(result.content[0]).isEqualTo(
        OrganisationSummary(
          organisationId = 123,
          organisationName = "Some name limited",
          organisationActive = true,
          flat = "Flat",
          property = "Property",
          street = "Street",
          area = "Area",
          cityCode = "123",
          cityDescription = "City",
          countyCode = "C.OUNTY",
          countyDescription = "County",
          postcode = "AB12 3CD",
          countryCode = "COU",
          countryDescription = "Country",
          businessPhoneNumber = "0123456",
          businessPhoneNumberExtension = "789",
        ),
      )
    }
  }

  companion object {
    @JvmStatic
    fun createOrganisationEntity(
      organisationId: Long = 1L,
      organisationName: String = "Name",
      programmeNumber: String = "P1",
      vatNumber: String = "V1",
      caseloadId: String = "C1",
      comments: String = "C2",
      active: Boolean = false,
      deactivatedDate: LocalDate? = null,
      createdBy: String = "Created by",
      createdTime: LocalDateTime = LocalDateTime.now(),
      updatedBy: String = "U1",
      updatedTime: LocalDateTime? = null,
    ) = OrganisationEntity(
      organisationId = organisationId,
      organisationName = organisationName,
      programmeNumber = programmeNumber,
      vatNumber = vatNumber,
      caseloadId = caseloadId,
      comments = comments,
      active = active,
      deactivatedDate = deactivatedDate,
      createdBy = createdBy,
      createdTime = createdTime,
      updatedBy = updatedBy,
      updatedTime = updatedTime,
    )
  }
}
