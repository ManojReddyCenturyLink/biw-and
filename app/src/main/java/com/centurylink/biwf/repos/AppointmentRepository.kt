package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.AppointmentResponse
import com.centurylink.biwf.model.appointment.AppointmentSlots
import com.centurylink.biwf.model.appointment.Appointments
import com.centurylink.biwf.model.appointment.RescheduleInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.service.network.AppointmentService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

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

            "SELECT Id, ArrivalWindowEndTime, ArrivalWindowStartTime, Status, Job_Type__c, WorkTypeId, Latitude, Longitude, ServiceTerritory.OperatingHours.TimeZone, (SELECT ServiceResource.Id, ServiceResource.Name FROM ServiceAppointment.ServiceResources) FROM ServiceAppointment WHERE AccountId = '%s'"
        val accountId = getAccountId()
        if (accountId.isNullOrEmpty()) {
            return Either.Left("Account ID is not available")
        }
        val finalQuery = String.format(query, accountId)
        val result: FiberServiceResult<Appointments> =
            appointmentService.getAppointmentDetails(finalQuery)
//        val result: FiberServiceResult<Appointments> =
//            integrationRestServices.getAppointmentDetails("appointmentDetails")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val appointmentRecords = it.records.elementAtOrElse(0) { null }
            appointmentRecords?.let { it ->
                val serviceRecords = it.serviceResources?.records?.elementAtOrElse(0) { null }
                var timeZoneInfo = it.serviceTerritory?.operatingHours?.timeZone
                if (timeZoneInfo.isNullOrEmpty()) {
                    //TODO: currently for few account getting timezone as null, need to check
                    timeZoneInfo = "America/Denver"
                }
                if (it.id.isNullOrEmpty()) {
                    Either.Left("Appointment Records is Empty")
                } else if (it.JobType.isNullOrEmpty() || it.arrivalWindowStarTime == null || it.arrivalWindowEndTime == null) {
                    Either.Left("Mandatory Records  is Empty")
                } else if (it.appointmentStatus == null || it.serviceResources == null) {
                    //TODO: Will remove when api gives correct response for cancelled state
                    val uiAppointmentRecords = AppointmentRecordsInfo(
                        serviceAppointmentStartDate = it.arrivalWindowStarTime,
                        serviceAppointmentEndTime = it.arrivalWindowEndTime,
                        serviceStatus = ServiceStatus.CANCELED,
                        serviceEngineerProfilePic = "",
                        jobType = "",
                        serviceLatitude = "",
                        serviceLongitude = "",
                        appointmentId = "",
                    serviceEngineerName = "",timeZone = timeZoneInfo!! )
                    Either.Right(uiAppointmentRecords)
                } else {
                    val engineerName = serviceRecords?.serviceResource?.name
                    val uiAppointmentRecords = AppointmentRecordsInfo(
                        serviceAppointmentStartDate = it.arrivalWindowStarTime,
                        serviceAppointmentEndTime = it.arrivalWindowEndTime,
                        serviceEngineerName = engineerName!!,
                        serviceStatus = it.appointmentStatus,
                        serviceEngineerProfilePic = "",
                        jobType = it.JobType,
                        serviceLatitude = it.latitude,
                        serviceLongitude = it.longitude,
                        appointmentId = it.id,
                        timeZone = timeZoneInfo!!
                    )
                    Either.Right(uiAppointmentRecords)
                }
            } ?: Either.Left("Appointment Records is Empty")
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
}