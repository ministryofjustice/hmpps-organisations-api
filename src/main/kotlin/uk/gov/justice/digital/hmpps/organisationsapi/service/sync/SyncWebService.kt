package uk.gov.justice.digital.hmpps.organisationsapi.service.sync

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toEntity
import uk.gov.justice.digital.hmpps.organisationsapi.mapping.sync.toModel
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncWebResponse
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWebAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository

@Service
@Transactional
class SyncWebService(
  val organisationRepository: OrganisationWithFixedIdRepository,
  val organisationWebAddressRepository: OrganisationWebAddressRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getWebAddressById(organisationWebId: Long): SyncWebResponse {
    val webEntity = organisationWebAddressRepository.findById(organisationWebId)
      .orElseThrow { EntityNotFoundException("Organisation web address with ID $organisationWebId not found") }
    return webEntity.toModel()
  }

  fun createWeb(request: SyncCreateWebRequest): SyncWebResponse {
    organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }
    return organisationWebAddressRepository.saveAndFlush(request.toEntity()).toModel()
  }

  fun updateWeb(organisationWebId: Long, request: SyncUpdateWebRequest): SyncWebResponse {
    val organisationEntity = organisationRepository.findById(request.organisationId)
      .orElseThrow { EntityNotFoundException("Organisation with ID ${request.organisationId} not found") }

    val webEntity = organisationWebAddressRepository.findById(organisationWebId)
      .orElseThrow { EntityNotFoundException("Organisation web address with ID $organisationWebId not found") }

    if (organisationEntity.organisationId != webEntity.organisationId) {
      logger.error("Organisation web address update for an organisation which is not linked to this web address")
      throw ValidationException("Organisation ID ${organisationEntity.organisationId} is not linked to web address ${webEntity.organisationWebAddressId}")
    }

    val changedWeb = webEntity.copy(
      organisationId = request.organisationId,
      webAddress = request.webAddress,
      updatedBy = request.updatedBy,
      updatedTime = request.updatedTime,
    )

    return organisationWebAddressRepository.saveAndFlush(changedWeb).toModel()
  }

  fun deleteWeb(organisationWebId: Long): SyncWebResponse {
    val webEntity = organisationWebAddressRepository.findById(organisationWebId)
      .orElseThrow { EntityNotFoundException("Organisation web address with ID $organisationWebId not found") }
    organisationWebAddressRepository.deleteById(organisationWebId)
    return webEntity.toModel()
  }
}
