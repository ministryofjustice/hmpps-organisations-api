package uk.gov.justice.digital.hmpps.organisationsapi.facade

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncOrganisationType
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateAddressRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateEmailRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateTypesRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateWebRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncAddressResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncEmailResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncTypesResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncWebResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncAddressService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncEmailService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncOrganisationService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncPhoneService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncTypesService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncWebService
import java.time.LocalDateTime

class SyncFacadeTest {
  private val syncOrganisationService: SyncOrganisationService = mock()
  private val syncPhoneService: SyncPhoneService = mock()
  private val syncEmailService: SyncEmailService = mock()
  private val syncWebService: SyncWebService = mock()
  private val syncAddressService: SyncAddressService = mock()
  private val syncTypesService: SyncTypesService = mock()
  private val outboundEventsService: OutboundEventsService = mock()

  private val facade = SyncFacade(
    syncOrganisationService,
    syncPhoneService,
    syncEmailService,
    syncWebService,
    syncAddressService,
    syncTypesService,
    outboundEventsService,
  )

  @Nested
  inner class SyncOrganisationFacadeEvents {
    @Test
    fun `should send ORGANISATION_CREATED domain event on create success`() {
      val request = syncCreateOrganisationRequest(organisationId = 1L)
      val response = syncOrganisationResponse(1L)

      whenever(syncOrganisationService.createOrganisation(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.createOrganisation(request)

      assertThat(result.organisationId).isEqualTo(request.organisationId)

      verify(syncOrganisationService).createOrganisation(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_CREATED,
        identifier = result.organisationId,
        organisationId = result.organisationId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_CREATED domain event on create failure`() {
      val request = syncCreateOrganisationRequest(organisationId = 2L)
      val expectedException = RuntimeException("Bang!")

      whenever(syncOrganisationService.createOrganisation(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.createOrganisation(request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncOrganisationService).createOrganisation(request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_UPDATED domain event on update success`() {
      val request = syncUpdateOrganisationRequest(organisationId = 3L)
      val response = syncOrganisationResponse(3L)

      whenever(syncOrganisationService.updateOrganisation(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.updateOrganisation(3L, request)

      verify(syncOrganisationService).updateOrganisation(3L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_UPDATED,
        identifier = result.organisationId,
        organisationId = result.organisationId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_UPDATED domain event on update failure`() {
      val request = syncUpdateOrganisationRequest(organisationId = 4L)
      val expectedException = RuntimeException("Bang!")

      whenever(syncOrganisationService.updateOrganisation(any(), any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.updateOrganisation(4L, request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncOrganisationService).updateOrganisation(4L, request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_DELETED domain event on delete success`() {
      val response = syncOrganisationResponse(5L)
      whenever(syncOrganisationService.deleteOrganisation(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.deleteOrganisation(5L)

      verify(syncOrganisationService).deleteOrganisation(5L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_DELETED,
        identifier = result.organisationId,
        organisationId = result.organisationId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_DELETED on delete failure`() {
      val expectedException = RuntimeException("Bang!")

      whenever(syncOrganisationService.deleteOrganisation(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.deleteOrganisation(6L)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)
      verify(syncOrganisationService).deleteOrganisation(6L)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    private fun syncCreateOrganisationRequest(organisationId: Long) = SyncCreateOrganisationRequest(
      organisationId = organisationId,
      organisationName = "Some Organisation",
      active = true,
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun syncOrganisationResponse(organisationId: Long) = SyncOrganisationResponse(
      organisationId = organisationId,
      organisationName = "Some Organisation",
      active = true,
      createdBy = "CREATOR",
      createdTime = LocalDateTime.now(),
      updatedBy = null,
      updatedTime = null,
    )

    private fun syncUpdateOrganisationRequest(organisationId: Long) = SyncUpdateOrganisationRequest(
      organisationId = organisationId,
      organisationName = "Some Organisation",
      vatNumber = "GB11111111",
      active = true,
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )
  }

  @Nested
  inner class SyncPhoneFacadeEvents {
    @Test
    fun `should send ORGANISATION_PHONE_CREATED domain event on create success`() {
      val request = syncCreatePhoneRequest()
      val response = syncPhoneResponse(1L)

      whenever(syncPhoneService.createPhone(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.createPhone(request)

      assertThat(result.organisationId).isEqualTo(request.organisationId)
      assertThat(result.organisationPhoneId).isEqualTo(1L)

      verify(syncPhoneService).createPhone(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_PHONE_CREATED,
        organisationId = result.organisationId,
        identifier = result.organisationPhoneId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_PHONE_CREATED domain event on create failure`() {
      val request = syncCreatePhoneRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncPhoneService.createPhone(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.createPhone(request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncPhoneService).createPhone(request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_PHONE_UPDATED domain event on update success`() {
      val request = syncUpdatePhoneRequest()
      val response = syncPhoneResponse(2L)

      whenever(syncPhoneService.updatePhone(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.updatePhone(2L, request)

      verify(syncPhoneService).updatePhone(2L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_PHONE_UPDATED,
        organisationId = result.organisationId,
        identifier = result.organisationPhoneId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_PHONE_UPDATED domain event on update failure`() {
      val request = syncUpdatePhoneRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncPhoneService.updatePhone(any(), any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.updatePhone(3L, request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncPhoneService).updatePhone(3L, request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_PHONE_DELETED domain event on delete success`() {
      val response = syncPhoneResponse(4L)
      whenever(syncPhoneService.deletePhone(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.deletePhone(4L)

      verify(syncPhoneService).deletePhone(4L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_PHONE_DELETED,
        organisationId = result.organisationId,
        identifier = result.organisationPhoneId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_PHONE_DELETED on delete failure`() {
      val expectedException = RuntimeException("Bang!")

      whenever(syncPhoneService.deletePhone(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.deletePhone(5L)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)
      verify(syncPhoneService).deletePhone(5L)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    private fun syncCreatePhoneRequest() = SyncCreatePhoneRequest(
      organisationId = 1L,
      phoneType = "MOB",
      phoneNumber = "07999 123456",
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun syncPhoneResponse(organisationPhoneId: Long) = SyncPhoneResponse(
      organisationId = 1L,
      organisationPhoneId = organisationPhoneId,
      phoneType = "MOB",
      phoneNumber = "07999 123456",
      createdBy = "CREATOR",
      createdTime = LocalDateTime.now(),
    )

    private fun syncUpdatePhoneRequest() = SyncUpdatePhoneRequest(
      organisationId = 1L,
      phoneType = "HOME",
      phoneNumber = "07999 654321",
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )
  }

  @Nested
  inner class SyncEmailFacadeEvents {
    @Test
    fun `should send ORGANISATION_EMAIL_CREATED domain event on create success`() {
      val request = syncCreateEmailRequest()
      val response = syncEmailResponse(1L)

      whenever(syncEmailService.createEmail(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.createEmail(request)

      assertThat(result.organisationId).isEqualTo(request.organisationId)
      assertThat(result.organisationEmailId).isEqualTo(1L)

      verify(syncEmailService).createEmail(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_EMAIL_CREATED,
        organisationId = result.organisationId,
        identifier = result.organisationEmailId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_EMAIL_CREATED domain event on create failure`() {
      val request = syncCreateEmailRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncEmailService.createEmail(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.createEmail(request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncEmailService).createEmail(request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_EMAIL_UPDATED domain event on update success`() {
      val request = syncUpdateEmailRequest()
      val response = syncEmailResponse(2L)

      whenever(syncEmailService.updateEmail(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.updateEmail(2L, request)

      verify(syncEmailService).updateEmail(2L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_EMAIL_UPDATED,
        organisationId = result.organisationId,
        identifier = result.organisationEmailId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_EMAIL_UPDATED domain event on update failure`() {
      val request = syncUpdateEmailRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncEmailService.updateEmail(any(), any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.updateEmail(3L, request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncEmailService).updateEmail(3L, request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_EMAIL_DELETED domain event on delete success`() {
      val response = syncEmailResponse(4L)
      whenever(syncEmailService.deleteEmail(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.deleteEmail(4L)

      verify(syncEmailService).deleteEmail(4L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_EMAIL_DELETED,
        organisationId = result.organisationId,
        identifier = result.organisationEmailId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_EMAIL_DELETED on delete failure`() {
      val expectedException = RuntimeException("Bang!")

      whenever(syncEmailService.deleteEmail(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.deleteEmail(5L)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)
      verify(syncEmailService).deleteEmail(5L)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    private fun syncCreateEmailRequest() = SyncCreateEmailRequest(
      organisationId = 1L,
      emailAddress = "created@example.com",
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun syncEmailResponse(organisationEmailId: Long) = SyncEmailResponse(
      organisationId = 1L,
      organisationEmailId = organisationEmailId,
      emailAddress = "created@example.com",
      createdBy = "CREATOR",
      createdTime = LocalDateTime.now(),
    )

    private fun syncUpdateEmailRequest() = SyncUpdateEmailRequest(
      organisationId = 1L,
      emailAddress = "updated@example.com",
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )
  }

  @Nested
  inner class SyncWebFacadeEvents {
    @Test
    fun `should send ORGANISATION_WEB_CREATED domain event on create success`() {
      val request = syncCreateWebRequest()
      val response = syncWebResponse(1L)

      whenever(syncWebService.createWeb(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.createWeb(request)

      assertThat(result.organisationId).isEqualTo(request.organisationId)
      assertThat(result.organisationWebAddressId).isEqualTo(1L)

      verify(syncWebService).createWeb(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_WEB_CREATED,
        organisationId = result.organisationId,
        identifier = result.organisationWebAddressId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_WEB_CREATED domain event on create failure`() {
      val request = syncCreateWebRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncWebService.createWeb(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.createWeb(request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncWebService).createWeb(request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_WEB_UPDATED domain event on update success`() {
      val request = syncUpdateWebRequest()
      val response = syncWebResponse(2L)

      whenever(syncWebService.updateWeb(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.updateWeb(2L, request)

      verify(syncWebService).updateWeb(2L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_WEB_UPDATED,
        organisationId = result.organisationId,
        identifier = result.organisationWebAddressId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_WEB_UPDATED domain event on update failure`() {
      val request = syncUpdateWebRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncWebService.updateWeb(any(), any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.updateWeb(3L, request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncWebService).updateWeb(3L, request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_WEB_DELETED domain event on delete success`() {
      val response = syncWebResponse(4L)
      whenever(syncWebService.deleteWeb(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.deleteWeb(4L)

      verify(syncWebService).deleteWeb(4L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_WEB_DELETED,
        organisationId = result.organisationId,
        identifier = result.organisationWebAddressId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_WEB_DELETED on delete failure`() {
      val expectedException = RuntimeException("Bang!")

      whenever(syncWebService.deleteWeb(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.deleteWeb(5L)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)
      verify(syncWebService).deleteWeb(5L)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    private fun syncCreateWebRequest() = SyncCreateWebRequest(
      organisationId = 1L,
      webAddress = "www.example.com",
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun syncWebResponse(organisationWebId: Long) = SyncWebResponse(
      organisationId = 1L,
      organisationWebAddressId = organisationWebId,
      webAddress = "www.example.com",
      createdBy = "CREATOR",
      createdTime = LocalDateTime.now(),
    )

    private fun syncUpdateWebRequest() = SyncUpdateWebRequest(
      organisationId = 1L,
      webAddress = "www.updated.com",
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )
  }

  @Nested
  inner class SyncAddressFacadeEvents {
    @Test
    fun `should send ORGANISATION_ADDRESS_CREATED domain event on create success`() {
      val request = syncCreateAddressRequest()
      val response = syncAddressResponse(1L)

      whenever(syncAddressService.createAddress(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.createAddress(request)

      assertThat(result.organisationId).isEqualTo(request.organisationId)
      assertThat(result.organisationAddressId).isEqualTo(1L)

      verify(syncAddressService).createAddress(request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_CREATED,
        organisationId = result.organisationId,
        identifier = result.organisationAddressId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_ADDRESS_CREATED domain event on create failure`() {
      val request = syncCreateAddressRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncAddressService.createAddress(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.createAddress(request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncAddressService).createAddress(request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_ADDRESS_UPDATED domain event on update success`() {
      val request = syncUpdateAddressRequest()
      val response = syncAddressResponse(2L)

      whenever(syncAddressService.updateAddress(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.updateAddress(2L, request)

      verify(syncAddressService).updateAddress(2L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_UPDATED,
        organisationId = result.organisationId,
        identifier = result.organisationAddressId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_ADDRESS_UPDATED domain event on update failure`() {
      val request = syncUpdateAddressRequest()
      val expectedException = RuntimeException("Bang!")

      whenever(syncAddressService.updateAddress(any(), any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.updateAddress(3L, request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncAddressService).updateAddress(3L, request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    @Test
    fun `should send ORGANISATION_ADDRESS_DELETED domain event on delete success`() {
      val response = syncAddressResponse(4L)
      whenever(syncAddressService.deleteAddress(any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.deleteAddress(4L)

      verify(syncAddressService).deleteAddress(4L)

      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_ADDRESS_DELETED,
        organisationId = result.organisationId,
        identifier = result.organisationAddressId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_ADDRESS_DELETED on delete failure`() {
      val expectedException = RuntimeException("Bang!")

      whenever(syncAddressService.deleteAddress(any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.deleteAddress(5L)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)
      verify(syncAddressService).deleteAddress(5L)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    private fun syncCreateAddressRequest() = SyncCreateAddressRequest(
      organisationId = 1L,
      addressType = "HOME",
      createdTime = LocalDateTime.now(),
      createdBy = "CREATOR",
    )

    private fun syncAddressResponse(organisationAddressId: Long) = SyncAddressResponse(
      organisationId = 1L,
      organisationAddressId = organisationAddressId,
      primaryAddress = true,
      mailAddress = true,
      serviceAddress = false,
      noFixedAddress = false,
      createdBy = "CREATOR",
      createdTime = LocalDateTime.now(),
    )

    private fun syncUpdateAddressRequest() = SyncUpdateAddressRequest(
      organisationId = 1L,
      addressType = "BUS",
      updatedBy = "UPDATER",
      updatedTime = LocalDateTime.now(),
    )
  }

  @Nested
  inner class SyncTypesFacadeEvents {
    @Test
    fun `should send ORGANISATION_TYPES_UPDATED domain event on update success`() {
      val request = syncUpdateTypesRequest(1L)
      val response = syncTypesResponse(1L)

      whenever(syncTypesService.updateTypes(any(), any())).thenReturn(response)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val result = facade.updateTypes(1L, request)

      verify(syncTypesService).updateTypes(1L, request)
      verify(outboundEventsService).send(
        outboundEvent = OutboundEvent.ORGANISATION_TYPES_UPDATED,
        organisationId = result.organisationId,
        identifier = result.organisationId,
        source = Source.NOMIS,
      )
    }

    @Test
    fun `should not send ORGANISATION_TYPES_UPDATED domain event on update failure`() {
      val request = syncUpdateTypesRequest(2L)
      val expectedException = RuntimeException("Bang!")

      whenever(syncTypesService.updateTypes(any(), any())).thenThrow(expectedException)
      whenever(outboundEventsService.send(any(), any(), any(), any())).then {}

      val exception = assertThrows<RuntimeException> {
        facade.updateTypes(2L, request)
      }

      assertThat(exception.message).isEqualTo(expectedException.message)

      verify(syncTypesService).updateTypes(2L, request)
      verify(outboundEventsService, never()).send(any(), any(), any(), any())
    }

    private fun syncUpdateTypesRequest(organisationId: Long) = SyncUpdateTypesRequest(
      organisationId = organisationId,
      types = listOf(
        SyncOrganisationType(type = "A", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
        SyncOrganisationType(type = "B", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
      ),
    )
    private fun syncTypesResponse(organisationId: Long) = SyncTypesResponse(
      organisationId = organisationId,
      types = listOf(
        SyncOrganisationType(type = "A", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
        SyncOrganisationType(type = "B", createdBy = "CREATOR", createdTime = LocalDateTime.now()),
      ),
    )
  }
}
