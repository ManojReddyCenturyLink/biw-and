package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.*
import com.centurylink.biwf.service.network.AppointmentService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.preferences.Preferences
import org.threeten.bp.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository class for all appointment  related API calls with suspend functions
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val preferences: Preferences,
    private val appointmentService: AppointmentService,
    private val integrationRestServices: IntegrationRestServices
) {

    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    suspend fun getAppointmentInfo(): Either<String, AppointmentRecordsInfo> {
        val query =
            "SELECT Id, ArrivalWindowEndTime, ArrivalWindowStartTime, Status, Job_Type__c, WorkTypeId, Latitude, Longitude, ServiceTerritory.OperatingHours.TimeZone,Appointment_Number_Text__c,(SELECT ServiceResource.Id, ServiceResource.Name FROM ServiceAppointment.ServiceResources) FROM ServiceAppointment WHERE AccountId = '%s'"
        val accountId = getAccountId()
        if (accountId.isNullOrEmpty()) {
            return Either.Left("Account ID is not available")
        }
        val finalQuery = String.format(query, accountId)
        val result: FiberServiceResult<Appointments> =
            appointmentService.getAppointmentDetails(finalQuery)
         // val result: FiberServiceResult<Appointments> =
           // integrationRestServices.getAppointmentDetails("appointmentDetails")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val appointmentRecords = it.records?.elementAtOrElse(0) { null }
            appointmentRecords?.let { it ->
                val serviceRecords = it.serviceResources?.records?.elementAtOrElse(0) { null }
                var timeZoneInfo = it.serviceTerritory?.operatingHours?.timeZone
                if (timeZoneInfo.isNullOrEmpty()) {
                    //TODO: currently for few account getting timezone as null, need to check
                    timeZoneInfo = "America/Denver"
                }
                if (it.id.isNullOrEmpty()) {
                    Either.Left("Appointment id is Empty")
                } else if (it.JobType.isNullOrEmpty() || it.appointmentNumber == null || it.appointmentStatus == null) {
                    Either.Left("Mandatory Records  is Empty")
                } else {

                    val engineerName = serviceRecords?.serviceResource?.name ?: ""
                    val uiAppointmentRecords = AppointmentRecordsInfo(
                        serviceAppointmentStartDate = it.arrivalWindowStarTime ?: LocalDateTime.MAX,
                        serviceAppointmentEndTime = it.arrivalWindowEndTime ?: LocalDateTime.MAX,
                        serviceEngineerName = engineerName,
                        serviceStatus = it.appointmentStatus,
                        serviceEngineerProfilePic = "",
                        jobType = it.JobType,
                        serviceLatitude = it.latitude?:"0.0",
                        serviceLongitude = it.longitude?:"0.0",
                        appointmentId = it.id,
                        timeZone = timeZoneInfo,
                        appointmentNumber = it.appointmentNumber
                    )
                    Either.Right(uiAppointmentRecords)
                }
            } ?: Either.Left("No Appointment Records")
        }
    }

    suspend fun getAppointmentSlots(
        serviceAppointmentId: String,
        expectedMinimalDate: String
    ): Either<String, AppointmentSlots> {
        // val result: FiberServiceResult<AppointmentSlots> =
        //   integrationRestServices.getAppointmentSlots("appointmentDetails")
        val result: FiberServiceResult<AppointmentSlots> =
            appointmentService.getAppointmentSlots(serviceAppointmentId, expectedMinimalDate)
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun modifyAppointmentInfo(rescheduleInfo: RescheduleInfo): Either<String, AppointmentResponse> {
        //val result: FiberServiceResult<AppointmentResponse> =
        //  integrationRestServices.submitAppointments(rescheduleInfo)
        val result: FiberServiceResult<AppointmentResponse> =
            appointmentService.reScheduleAppointment(rescheduleInfo)
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun cancelAppointment(cancelAppointmentInfo: CancelAppointmentInfo): Either<String, CancelResponse> {
        //val result: FiberServiceResult<AppointmentResponse> =
        //  integrationRestServices.submitAppointments(rescheduleInfo)
        val result: FiberServiceResult<CancelResponse> =
            appointmentService.cancelAppointment(cancelAppointmentInfo)
        return result.mapLeft { it.message?.message.toString() }
    }
}