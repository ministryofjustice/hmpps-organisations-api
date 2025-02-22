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
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncCreatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdateOrganisationRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.request.sync.SyncUpdatePhoneRequest
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncOrganisationResponse
import uk.gov.justice.digital.hmpps.organisationsapi.model.response.sync.SyncPhoneResponse
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEvent
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.organisationsapi.service.events.Source
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncOrganisationService
import uk.gov.justice.digital.hmpps.organisationsapi.service.sync.SyncPhoneService
import java.time.LocalDateTime

class SyncFacadeTest {
  private val syncOrganisationService: SyncOrganisationService = mock()
  private val syncPhoneService: SyncPhoneService = mock()
  private val outboundEventsService: OutboundEventsService = mock()

  private val facade = SyncFacade(
    syncOrganisationService,
    syncPhoneService,
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
}
