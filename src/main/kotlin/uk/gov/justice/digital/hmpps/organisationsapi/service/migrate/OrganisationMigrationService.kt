package uk.gov.justice.digital.hmpps.organisationsapi.service.migrate

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationAddressPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationEmailEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationPhoneEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationTypeId
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWebAddressEntity
import uk.gov.justice.digital.hmpps.organisationsapi.entity.OrganisationWithFixedIdEntity
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationEmailAddress
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationPhoneNumber
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationType
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationWebAddress
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate.ElementType
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate.IdPair
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate.MigrateOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate.MigratedOrganisationAddress
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationEmailRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWebAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWithFixedIdRepository
import java.time.LocalDateTime

@Service
class OrganisationMigrationService(
  private val organisationRepository: OrganisationWithFixedIdRepository,
  private val organisationTypeRepository: OrganisationTypeRepository,
  private val organisationPhoneRepository: OrganisationPhoneRepository,
  private val organisationEmailRepository: OrganisationEmailRepository,
  private val organisationWebAddressRepository: OrganisationWebAddressRepository,
  private val organisationAddressRepository: OrganisationAddressRepository,
  private val organisationAddressPhoneRepository: OrganisationAddressPhoneRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun migrateOrganisation(request: MigrateOrganisationRequest): MigrateOrganisationResponse {
    logger.info(
      "Migrate organisation ID {} with {} addresses, {} phones, {} emails, {} web addresses, {} types",
      request.nomisCorporateId,
      request.addresses.size,
      request.phoneNumbers.size,
      request.emailAddresses.size,
      request.webAddresses.size,
      request.organisationTypes.size,
    )

    if (organisationRepository.existsById(request.nomisCorporateId)) {
      logger.info("Migration: Duplicate corporate ID received ${request.nomisCorporateId} - replacing it")
      removeExistingOrgAndDetail(request.nomisCorporateId)
    }

    val organisation = createOrganisation(request)
    val createdOrganisation = IdPair(
      ElementType.ORGANISATION,
      request.nomisCorporateId,
      organisation.organisationId,
    )
    val createdOrganisationTypes = request.organisationTypes.map {
      createOrganisationType(organisation, it).id.organisationType
    }
    val createdPhoneNumbers = request.phoneNumbers.map {
      IdPair(
        ElementType.PHONE,
        it.nomisPhoneId,
        createOrganisationPhoneNumber(organisation, it).organisationPhoneId,
      )
    }
    val createdEmailAddresses = request.emailAddresses.map {
      IdPair(
        ElementType.EMAIL,
        it.nomisEmailAddressId,
        createOrganisationEmail(organisation, it).organisationEmailId,
      )
    }
    val createdWebAddresses = request.webAddresses.map {
      IdPair(
        ElementType.WEB_ADDRESS,
        it.nomisWebAddressId,
        createOrganisationWebAddress(organisation, it).organisationWebAddressId,
      )
    }

    val createdAddresses = request.addresses.map {
      val address = organisationAddressRepository.saveAndFlush(
        OrganisationAddressEntity(
          organisationAddressId = 0,
          organisationId = organisation.id(),
          addressType = it.type,
          primaryAddress = it.primaryAddress,
          mailAddress = it.mailAddress,
          serviceAddress = it.serviceAddress,
          noFixedAddress = it.noFixedAddress,
          flat = it.flat,
          property = it.premise,
          street = it.street,
          area = it.locality,
          cityCode = it.city,
          countyCode = it.county,
          postCode = it.postCode,
          countryCode = it.country,
          specialNeedsCode = it.specialNeedsCode,
          contactPersonName = it.contactPersonName,
          businessHours = it.businessHours,
          comments = it.comment,
          startDate = it.startDate,
          endDate = it.endDate,
          createdBy = it.createUsername ?: "MIGRATION",
          createdTime = it.createDateTime ?: LocalDateTime.now(),
          updatedBy = it.modifyUsername,
          updatedTime = it.modifyDateTime,
        ),
      )
      MigratedOrganisationAddress(
        IdPair(
          ElementType.ADDRESS,
          it.nomisAddressId,
          address.organisationAddressId,
        ),
        phoneNumbers = it.phoneNumbers.map { addressPhoneRequest ->
          val phone = createOrganisationPhoneNumber(organisation, addressPhoneRequest)
          IdPair(
            ElementType.PHONE,
            addressPhoneRequest.nomisPhoneId,
            createOrganisationAddressPhone(organisation, phone, address, addressPhoneRequest).organisationAddressPhoneId,
          )
        },
      )
    }
    return MigrateOrganisationResponse(
      createdOrganisation,
      createdOrganisationTypes,
      createdPhoneNumbers,
      createdEmailAddresses,
      createdWebAddresses,
      createdAddresses,
    )
  }

  private fun removeExistingOrgAndDetail(organisationId: Long) {
    organisationAddressPhoneRepository.deleteAllByOrganisationId(organisationId)
    organisationAddressRepository.deleteAllByOrganisationId(organisationId)
    organisationPhoneRepository.deleteAllByOrganisationId(organisationId)
    organisationEmailRepository.deleteAllByOrganisationId(organisationId)
    organisationWebAddressRepository.deleteAllByOrganisationId(organisationId)
    organisationTypeRepository.deleteAllByOrganisationId(organisationId)
    organisationRepository.deleteById(organisationId)
  }

  private fun createOrganisationAddressPhone(
    organisation: OrganisationWithFixedIdEntity,
    phone: OrganisationPhoneEntity,
    address: OrganisationAddressEntity,
    it: MigrateOrganisationPhoneNumber,
  ) = organisationAddressPhoneRepository.saveAndFlush(
    OrganisationAddressPhoneEntity(
      organisationAddressPhoneId = 0,
      organisationId = organisation.id(),
      organisationPhoneId = phone.organisationPhoneId,
      organisationAddressId = address.organisationAddressId,
      createdBy = it.createUsername ?: "MIGRATION",
      createdTime = it.createDateTime ?: LocalDateTime.now(),
      updatedBy = it.modifyUsername,
      updatedTime = it.modifyDateTime,
    ),
  )

  private fun createOrganisation(request: MigrateOrganisationRequest) = organisationRepository.saveAndFlush(
    OrganisationWithFixedIdEntity(
      request.nomisCorporateId,
      organisationName = request.organisationName,
      programmeNumber = request.programmeNumber,
      vatNumber = request.vatNumber,
      caseloadId = request.caseloadId,
      comments = request.comments,
      active = request.active,
      deactivatedDate = request.deactivatedDate,
      createdBy = request.createUsername ?: "MIGRATION",
      createdTime = request.createDateTime ?: LocalDateTime.now(),
      updatedBy = request.modifyUsername,
      updatedTime = request.modifyDateTime,
    ),
  )

  private fun createOrganisationType(
    organisation: OrganisationWithFixedIdEntity,
    it: MigrateOrganisationType,
  ) = organisationTypeRepository.saveAndFlush(
    OrganisationTypeEntity(
      OrganisationTypeId(
        organisationId = organisation.id(),
        organisationType = it.type,
      ),
      createdBy = it.createUsername ?: "MIGRATION",
      createdTime = it.createDateTime ?: LocalDateTime.now(),
      updatedBy = it.modifyUsername,
      updatedTime = it.modifyDateTime,
    ),
  )

  private fun createOrganisationPhoneNumber(
    organisation: OrganisationWithFixedIdEntity,
    it: MigrateOrganisationPhoneNumber,
  ) = organisationPhoneRepository.save(
    OrganisationPhoneEntity(
      organisationPhoneId = 0,
      organisationId = organisation.id(),
      phoneType = it.type,
      phoneNumber = it.number,
      extNumber = it.extension,
      createdBy = it.createUsername ?: "MIGRATION",
      createdTime = it.createDateTime ?: LocalDateTime.now(),
      updatedBy = it.modifyUsername,
      updatedTime = it.modifyDateTime,
    ),
  )

  private fun createOrganisationEmail(
    organisation: OrganisationWithFixedIdEntity,
    it: MigrateOrganisationEmailAddress,
  ) = organisationEmailRepository.saveAndFlush(
    OrganisationEmailEntity(
      organisationEmailId = 0,
      organisationId = organisation.id(),
      emailAddress = it.email,
      createdBy = it.createUsername ?: "MIGRATION",
      createdTime = it.createDateTime ?: LocalDateTime.now(),
      updatedBy = it.modifyUsername,
      updatedTime = it.modifyDateTime,
    ),
  )

  private fun createOrganisationWebAddress(
    organisation: OrganisationWithFixedIdEntity,
    it: MigrateOrganisationWebAddress,
  ) = organisationWebAddressRepository.saveAndFlush(
    OrganisationWebAddressEntity(
      organisationWebAddressId = 0,
      organisationId = organisation.id(),
      webAddress = it.webAddress,
      createdBy = it.createUsername ?: "MIGRATION",
      createdTime = it.createDateTime ?: LocalDateTime.now(),
      updatedBy = it.modifyUsername,
      updatedTime = it.modifyDateTime,
    ),
  )
}
