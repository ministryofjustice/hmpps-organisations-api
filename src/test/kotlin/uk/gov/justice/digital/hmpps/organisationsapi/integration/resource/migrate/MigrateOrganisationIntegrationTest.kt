package uk.gov.justice.digital.hmpps.organisationsapi.integration.resource.migrate

import org.apache.commons.lang3.RandomUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.organisationsapi.integration.PostgresIntegrationTestBase
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationAddress
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationEmailAddress
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationPhoneNumber
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationType
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.migrate.MigrateOrganisationWebAddress
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.migrate.ElementType
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationAddressRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationEmailRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationPhoneRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationTypeRepository
import uk.gov.justice.digital.hmpps.organisationsapi.repository.OrganisationWebAddressRepository
import java.time.LocalDate
import java.time.LocalDateTime

class MigrateOrganisationIntegrationTest : PostgresIntegrationTestBase() {

  private val corporateId: Long = RandomUtils.secure().randomLong(1, 10000)

  @Autowired
  private lateinit var organisationRepository: OrganisationRepository

  @Autowired
  private lateinit var organisationTypeRepository: OrganisationTypeRepository

  @Autowired
  private lateinit var organisationPhoneRepository: OrganisationPhoneRepository

  @Autowired
  private lateinit var organisationEmailRepository: OrganisationEmailRepository

  @Autowired
  private lateinit var organisationWebAddressRepository: OrganisationWebAddressRepository

  @Autowired
  private lateinit var organisationAddressRepository: OrganisationAddressRepository

  @Autowired
  private lateinit var organisationAddressPhoneRepository: OrganisationAddressPhoneRepository

  @Test
  fun `should return unauthorized if no token provided`() {
    webTestClient.post()
      .uri("/migrate/organisation")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(minimalMigrationRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_ORGANISATIONS__R", "ROLE_ORGANISATIONS__RW"])
  fun `should return forbidden without an authorised role on the token`(authRole: String) {
    webTestClient.post()
      .uri("/migrate/organisation")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(minimalMigrationRequest())
      .headers(setAuthorisation(roles = listOf(authRole)))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `can migrate a minimal organisation`() {
    val request = minimalMigrationRequest()
    val countOrgsBefore = organisationRepository.count()

    val result = testAPIClient.migrateAnOrganisation(request)

    with(result.organisation) {
      assertThat(elementType).isEqualTo(ElementType.ORGANISATION)

      // NOTE: The DPS and NOMIS ids should both be the same
      assertThat(nomisId).isEqualTo(request.nomisCorporateId)
      assertThat(dpsId).isEqualTo(request.nomisCorporateId)
    }

    assertThat(organisationRepository.count()).isEqualTo(countOrgsBefore + 1)
  }

  @Test
  fun `can migrate an organisation with all top level details`() {
    val request = MigrateOrganisationRequest(
      nomisCorporateId = corporateId,
      organisationName = "Basic Org",
      programmeNumber = "P1",
      vatNumber = "VAT",
      caseloadId = "CAS",
      comments = "Comments",
      active = false,
      deactivatedDate = LocalDate.of(2020, 2, 3),
      organisationTypes = emptyList(),
      phoneNumbers = emptyList(),
      emailAddresses = emptyList(),
      webAddresses = emptyList(),
      addresses = emptyList(),
    ).apply {
      createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
      createUsername = "CREATED"
      modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
      modifyUsername = "MODIFIED"
    }

    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    with(organisationRepository.getReferenceById(corporateId)) {
      assertThat(organisationName).isEqualTo("Basic Org")
      assertThat(programmeNumber).isEqualTo("P1")
      assertThat(vatNumber).isEqualTo("VAT")
      assertThat(caseloadId).isEqualTo("CAS")
      assertThat(comments).isEqualTo("Comments")
      assertThat(active).isEqualTo(false)
      assertThat(deactivatedDate).isEqualTo(LocalDate.of(2020, 2, 3))
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isEqualTo(LocalDateTime.of(2020, 3, 4, 11, 45))
      assertThat(updatedBy).isEqualTo("MODIFIED")
    }
  }

  @Test
  fun `can migrate an organisation with organisation types`() {
    val request = minimalMigrationRequest().copy(
      organisationTypes = listOf(
        MigrateOrganisationType("BSKILLS").apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
          modifyUsername = "MODIFIED"
        },
        MigrateOrganisationType("TRUST").apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
          modifyUsername = "MODIFIED"
        },
      ),
    )
    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    assertThat(migrated.organisationTypes).isEqualTo(listOf("BSKILLS", "TRUST"))
  }

  @Test
  fun `can migrate an organisation with phone numbers`() {
    val request = minimalMigrationRequest().copy(
      phoneNumbers = listOf(
        MigrateOrganisationPhoneNumber(
          nomisPhoneId = 999,
          type = "MOB",
          number = "123",
          extension = null,
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = null
          modifyUsername = null
        },
        MigrateOrganisationPhoneNumber(
          nomisPhoneId = 888,
          type = "HOME",
          number = "456",
          extension = "789",
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
          modifyUsername = "MODIFIED"
        },
      ),
    )

    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    assertThat(migrated.phoneNumbers).hasSize(2)

    with(organisationPhoneRepository.getReferenceById(migrated.phoneNumbers.find { it.nomisId == 999L }!!.dpsId)) {
      assertThat(phoneType).isEqualTo("MOB")
      assertThat(phoneNumber).isEqualTo("123")
      assertThat(extNumber).isNull()
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isNull()
      assertThat(updatedBy).isNull()
    }
    with(organisationPhoneRepository.getReferenceById(migrated.phoneNumbers.find { it.nomisId == 888L }!!.dpsId)) {
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(phoneNumber).isEqualTo("456")
      assertThat(extNumber).isEqualTo("789")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isEqualTo(LocalDateTime.of(2020, 3, 4, 11, 45))
      assertThat(updatedBy).isEqualTo("MODIFIED")
    }
  }

  @Test
  fun `can migrate an organisation with email addresses`() {
    val request = minimalMigrationRequest().copy(
      emailAddresses = listOf(
        MigrateOrganisationEmailAddress(
          nomisEmailAddressId = 999,
          email = "test@example.com",
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = null
          modifyUsername = null
        },
        MigrateOrganisationEmailAddress(
          nomisEmailAddressId = 888,
          email = "another@example.com",
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
          modifyUsername = "MODIFIED"
        },
      ),
    )

    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    assertThat(migrated.emailAddresses).hasSize(2)

    with(organisationEmailRepository.getReferenceById(migrated.emailAddresses.find { it.nomisId == 999L }!!.dpsId)) {
      assertThat(emailAddress).isEqualTo("test@example.com")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isNull()
      assertThat(updatedBy).isNull()
    }
    with(organisationEmailRepository.getReferenceById(migrated.emailAddresses.find { it.nomisId == 888L }!!.dpsId)) {
      assertThat(emailAddress).isEqualTo("another@example.com")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isEqualTo(LocalDateTime.of(2020, 3, 4, 11, 45))
      assertThat(updatedBy).isEqualTo("MODIFIED")
    }
  }

  @Test
  fun `can migrate an organisation with web addresses`() {
    val request = minimalMigrationRequest().copy(
      webAddresses = listOf(
        MigrateOrganisationWebAddress(
          nomisWebAddressId = 999,
          webAddress = "test@example.com",
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = null
          modifyUsername = null
        },
        MigrateOrganisationWebAddress(
          nomisWebAddressId = 888,
          webAddress = "another@example.com",
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
          modifyUsername = "MODIFIED"
        },
      ),
    )

    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    assertThat(migrated.webAddresses).hasSize(2)

    with(organisationWebAddressRepository.getReferenceById(migrated.webAddresses.find { it.nomisId == 999L }!!.dpsId)) {
      assertThat(webAddress).isEqualTo("test@example.com")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isNull()
      assertThat(updatedBy).isNull()
    }
    with(organisationWebAddressRepository.getReferenceById(migrated.webAddresses.find { it.nomisId == 888L }!!.dpsId)) {
      assertThat(webAddress).isEqualTo("another@example.com")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isEqualTo(LocalDateTime.of(2020, 3, 4, 11, 45))
      assertThat(updatedBy).isEqualTo("MODIFIED")
    }
  }

  @Test
  fun `can migrate an organisation with addresses`() {
    val request = minimalMigrationRequest().copy(
      addresses = listOf(
        MigrateOrganisationAddress(
          nomisAddressId = 999,
          type = null,
          primaryAddress = false,
          mailAddress = false,
          serviceAddress = false,
          noFixedAddress = false,
          flat = null,
          premise = null,
          street = null,
          locality = null,
          city = null,
          county = null,
          postCode = null,
          country = null,
          specialNeedsCode = null,
          contactPersonName = null,
          businessHours = null,
          comment = null,
          startDate = null,
          endDate = null,
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = null
          modifyUsername = null
        },
        MigrateOrganisationAddress(
          nomisAddressId = 888,
          type = "HOME",
          primaryAddress = true,
          mailAddress = true,
          serviceAddress = true,
          noFixedAddress = true,
          flat = "F1",
          premise = "P1",
          street = "S1",
          locality = "A1",
          city = "25343",
          county = "S.YORKSHIRE",
          postCode = "P1C1",
          country = "ENG",
          specialNeedsCode = "DEAF",
          contactPersonName = "CP1",
          businessHours = "BH1",
          comment = "Comments",
        ).apply {
          createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
          createUsername = "CREATED"
          modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
          modifyUsername = "MODIFIED"
        },
      ),
    )

    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    assertThat(migrated.addresses).hasSize(2)

    with(organisationAddressRepository.getReferenceById(migrated.addresses.find { it.address.nomisId == 999L }!!.address.dpsId)) {
      assertThat(addressType).isNull()
      assertThat(primaryAddress).isFalse()
      assertThat(mailAddress).isFalse()
      assertThat(serviceAddress).isFalse()
      assertThat(noFixedAddress).isFalse()
      assertThat(flat).isNull()
      assertThat(property).isNull()
      assertThat(street).isNull()
      assertThat(area).isNull()
      assertThat(cityCode).isNull()
      assertThat(countyCode).isNull()
      assertThat(postCode).isNull()
      assertThat(countryCode).isNull()
      assertThat(specialNeedsCode).isNull()
      assertThat(contactPersonName).isNull()
      assertThat(businessHours).isNull()
      assertThat(comments).isNull()
      assertThat(startDate).isNull()
      assertThat(endDate).isNull()
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isNull()
      assertThat(updatedBy).isNull()
    }
    with(organisationAddressRepository.getReferenceById(migrated.addresses.find { it.address.nomisId == 888L }!!.address.dpsId)) {
      assertThat(addressType).isEqualTo("HOME")
      assertThat(primaryAddress).isTrue()
      assertThat(mailAddress).isTrue()
      assertThat(serviceAddress).isTrue()
      assertThat(noFixedAddress).isTrue()
      assertThat(flat).isEqualTo("F1")
      assertThat(property).isEqualTo("P1")
      assertThat(street).isEqualTo("S1")
      assertThat(area).isEqualTo("A1")
      assertThat(cityCode).isEqualTo("25343")
      assertThat(countyCode).isEqualTo("S.YORKSHIRE")
      assertThat(postCode).isEqualTo("P1C1")
      assertThat(countryCode).isEqualTo("ENG")
      assertThat(specialNeedsCode).isEqualTo("DEAF")
      assertThat(contactPersonName).isEqualTo("CP1")
      assertThat(businessHours).isEqualTo("BH1")
      assertThat(comments).isEqualTo("Comments")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isEqualTo(LocalDateTime.of(2020, 3, 4, 11, 45))
      assertThat(updatedBy).isEqualTo("MODIFIED")
    }
  }

  @Test
  fun `can migrate an organisation with address phone numbers`() {
    val request = minimalMigrationRequest().copy(
      addresses = listOf(
        MigrateOrganisationAddress(
          nomisAddressId = 999,
          type = null,
          primaryAddress = false,
          mailAddress = false,
          serviceAddress = false,
          noFixedAddress = false,
          flat = null,
          premise = null,
          street = null,
          locality = null,
          city = null,
          county = null,
          postCode = null,
          country = null,
          specialNeedsCode = null,
          contactPersonName = null,
          businessHours = null,
          comment = null,
          startDate = null,
          endDate = null,
          phoneNumbers = listOf(
            MigrateOrganisationPhoneNumber(
              nomisPhoneId = 123,
              type = "MOB",
              number = "123",
              extension = null,
            ).apply {
              createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
              createUsername = "CREATED"
              modifyDateTime = null
              modifyUsername = null
            },
            MigrateOrganisationPhoneNumber(
              nomisPhoneId = 456,
              type = "HOME",
              number = "456",
              extension = "789",
            ).apply {
              createDateTime = LocalDateTime.of(2020, 2, 3, 10, 30)
              createUsername = "CREATED"
              modifyDateTime = LocalDateTime.of(2020, 3, 4, 11, 45)
              modifyUsername = "MODIFIED"
            },
          ),
        ),
      ),
    )

    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    assertThat(migrated.addresses).hasSize(1)
    val migratedAddress = migrated.addresses.first()

    val firstPhone =
      organisationAddressPhoneRepository.getReferenceById(migratedAddress.phoneNumbers.find { it.nomisId == 123L }!!.dpsId)
    assertThat(firstPhone.organisationAddressId).isEqualTo(migratedAddress.address.dpsId)
    val secondPhone =
      organisationAddressPhoneRepository.getReferenceById(migratedAddress.phoneNumbers.find { it.nomisId == 456L }!!.dpsId)
    assertThat(secondPhone.organisationAddressId).isEqualTo(migratedAddress.address.dpsId)

    with(organisationPhoneRepository.getReferenceById(firstPhone.organisationPhoneId)) {
      assertThat(phoneType).isEqualTo("MOB")
      assertThat(phoneNumber).isEqualTo("123")
      assertThat(extNumber).isNull()
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isNull()
      assertThat(updatedBy).isNull()
    }
    with(organisationPhoneRepository.getReferenceById(secondPhone.organisationPhoneId)) {
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(phoneNumber).isEqualTo("456")
      assertThat(extNumber).isEqualTo("789")
      assertThat(createdTime).isEqualTo(LocalDateTime.of(2020, 2, 3, 10, 30))
      assertThat(createdBy).isEqualTo("CREATED")
      assertThat(updatedTime).isEqualTo(LocalDateTime.of(2020, 3, 4, 11, 45))
      assertThat(updatedBy).isEqualTo("MODIFIED")
    }
  }

  @Test
  fun `duplicate corporate id should remove existing entities and re-create`() {
    val request = minimalMigrationRequest().copy(
      organisationTypes = listOf(MigrateOrganisationType("BSKILLS")),
      phoneNumbers = listOf(
        MigrateOrganisationPhoneNumber(
          nomisPhoneId = 123,
          type = "MOB",
          number = "123",
          extension = null,
        ),
      ),
      emailAddresses = listOf(
        MigrateOrganisationEmailAddress(
          nomisEmailAddressId = 456,
          email = "test@example.com",
        ),
      ),
      webAddresses = listOf(
        MigrateOrganisationWebAddress(
          nomisWebAddressId = 789,
          webAddress = "test@example.com",
        ),
      ),
      addresses = listOf(
        MigrateOrganisationAddress(
          nomisAddressId = 369,
          type = null,
          primaryAddress = false,
          mailAddress = false,
          serviceAddress = false,
          noFixedAddress = false,
          flat = null,
          premise = null,
          street = null,
          locality = null,
          city = null,
          county = null,
          postCode = null,
          country = null,
          specialNeedsCode = null,
          contactPersonName = null,
          businessHours = null,
          comment = null,
          startDate = null,
          endDate = null,
          phoneNumbers = listOf(
            MigrateOrganisationPhoneNumber(
              nomisPhoneId = 248,
              type = "MOB",
              number = "123",
              extension = null,
            ),
          ),
        ),
      ),
    )

    val migrated = testAPIClient.migrateAnOrganisation(request)

    assertThat(migrated.organisation.dpsId).isEqualTo(corporateId)
    val originalDpsPhoneId = migrated.phoneNumbers.first().dpsId
    val originalDpsEmailId = migrated.emailAddresses.first().dpsId
    val originalDpsWebId = migrated.webAddresses.first().dpsId
    val originalDpsAddressId = migrated.addresses.first().address.dpsId
    val originalDpsAddressPhoneId = migrated.addresses.first().phoneNumbers.first().dpsId
    val originalDpsAddressPhonePhoneId =
      organisationAddressPhoneRepository.getReferenceById(originalDpsAddressPhoneId).organisationPhoneId

    assertThat(organisationRepository.existsById(migrated.organisation.dpsId)).isTrue()
    assertThat(organisationPhoneRepository.existsById(originalDpsPhoneId)).isTrue()
    assertThat(organisationPhoneRepository.existsById(originalDpsAddressPhonePhoneId)).isTrue()
    assertThat(organisationEmailRepository.existsById(originalDpsEmailId)).isTrue()
    assertThat(organisationWebAddressRepository.existsById(originalDpsWebId)).isTrue()
    assertThat(organisationAddressRepository.existsById(originalDpsAddressId)).isTrue()

    val newName = "A new name to check we updated org"
    val migratedAgain = testAPIClient.migrateAnOrganisation(request.copy(organisationName = newName))
    assertThat(migratedAgain.organisation.dpsId).isEqualTo(corporateId)
    val newDpsPhoneId = migratedAgain.phoneNumbers.first().dpsId
    val newDpsEmailId = migratedAgain.emailAddresses.first().dpsId
    val newDpsWebId = migratedAgain.webAddresses.first().dpsId
    val newDpsAddressId = migratedAgain.addresses.first().address.dpsId
    val newDpsAddressPhoneId = migratedAgain.addresses.first().phoneNumbers.first().dpsId
    val newDpsAddressPhonePhoneId =
      organisationAddressPhoneRepository.getReferenceById(newDpsAddressPhoneId).organisationPhoneId

    // old removed
    assertThat(organisationPhoneRepository.existsById(originalDpsPhoneId)).isFalse()
    assertThat(organisationPhoneRepository.existsById(originalDpsAddressPhonePhoneId)).isFalse()
    assertThat(organisationEmailRepository.existsById(originalDpsEmailId)).isFalse()
    assertThat(organisationWebAddressRepository.existsById(originalDpsWebId)).isFalse()
    assertThat(organisationAddressRepository.existsById(originalDpsAddressId)).isFalse()

    // new added
    assertThat(organisationRepository.getReferenceById(corporateId).organisationName).isEqualTo(newName)
    assertThat(organisationPhoneRepository.existsById(newDpsPhoneId)).isTrue()
    assertThat(organisationPhoneRepository.existsById(newDpsAddressPhonePhoneId)).isTrue()
    assertThat(organisationEmailRepository.existsById(newDpsEmailId)).isTrue()
    assertThat(organisationWebAddressRepository.existsById(newDpsWebId)).isTrue()
    assertThat(organisationAddressRepository.existsById(newDpsAddressId)).isTrue()
  }

  private fun minimalMigrationRequest(): MigrateOrganisationRequest = MigrateOrganisationRequest(
    nomisCorporateId = corporateId,
    organisationName = "Basic Org",
    programmeNumber = null,
    vatNumber = null,
    caseloadId = null,
    comments = null,
    active = true,
    deactivatedDate = null,
    organisationTypes = emptyList(),
    phoneNumbers = emptyList(),
    emailAddresses = emptyList(),
    webAddresses = emptyList(),
    addresses = emptyList(),
  )
}
