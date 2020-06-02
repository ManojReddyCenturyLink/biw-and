package com.centurylink.biwf.repos

import android.util.Log
import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.Appointments
import com.centurylink.biwf.model.appointment.ServiceAppointments
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
            "SELECT Id, ArrivalWindowEndTime, ArrivalWindowStartTime, Status, Job_Type__c, WorkTypeId, Latitude, Longitude, (SELECT ServiceResource.Id, ServiceResource.Name FROM ServiceAppointment.ServiceResources) FROM ServiceAppointment WHERE AccountId = '%s'"
        val accountId = getAccountId()
        if (accountId.isNullOrEmpty()) {
            Log.i("JAMMY","Account Id is Empty")
            return Either.Left("Account ID is not available")
        }
        val finalQuery = String.format(query, accountId)
        //  val result: FiberServiceResult<Appointments> =
        //    appointmentService.getAppointmentDetails(finalQuery)
        val result: FiberServiceResult<Appointments> =
            integrationRestServices.getAppointmentDetails("appointmentDetails")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val appointmentRecords = it.records.elementAtOrElse(0) { null }
            appointmentRecords?.let { it ->
                val serviceRecords = it.serviceResources?.records?.elementAtOrElse(0) { null }
                if (it.id.isNullOrEmpty()) {
                    Either.Left("Appointment Records is Empty")
                } else if (it.appointmentStatus == null || it.JobType.isNullOrEmpty() || serviceRecords?.serviceResource == null || it.arrivalWindowStarTime == null || it.arrivalWindowEndTime == null) {
                    Either.Left("Mandatory Records  is Empty")
                } else {
                    val uiAppointmentRecords = AppointmentRecordsInfo(
                        serviceAppointmentStartDate = it.arrivalWindowStarTime,
                        serviceAppointmentEndTime = it.arrivalWindowEndTime,
                        serviceEngineerName = serviceRecords.serviceResource.name!!,
                        serviceStatus = it.appointmentStatus,
                        serviceEngineerProfilePic = "",
                        jobType = it.JobType,
                        serviceLatitude = it.latitude,
                        serviceLongitude = it.longitude,
                        appointmentId = it.id
                    )
                    Either.Right(uiAppointmentRecords)
                }
            } ?: Either.Left("Appointment Records is Empty")
        }
    }

    suspend fun getAppointmentStatus(): Either<String, ServiceStatus> {
        val query =
            "SELECT Id, ArrivalWindowEndTime, ArrivalWindowStartTime, Status, Job_Type__c, WorkTypeId, Latitude, Longitude, (SELECT ServiceResource.Id, ServiceResource.Name FROM ServiceAppointment.ServiceResources) FROM ServiceAppointment WHERE AccountId = '%s'"
        val accountId = getAccountId()
        if (accountId.isNullOrEmpty()) {
            return Either.Left("Account ID is not available")
        }
        val finalQuery = String.format(query, accountId)
        //  val result: FiberServiceResult<Appointments> =
        //    appointmentService.getAppointmentDetails(finalQuery)
        val result: FiberServiceResult<Appointments> =
            integrationRestServices.getAppointmentDetails("appointmentDetails")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->

            val appointmentRecords = it.records.elementAtOrElse(0) { null }
            appointmentRecords?.let { it ->
                val serviceRecords = it.serviceResources?.records?.elementAtOrElse(0) { null }
                if (it.id.isNullOrEmpty()) {
                    Either.Left("Appointment Records is Empty")
                } else if (it.appointmentStatus == null) {
                    Either.Left("Appointment status   is Empty")
                } else {
                    Either.Right(it.appointmentStatus)
                }
            } ?: Either.Left("Appointment Records is Empty")
        }
    }

    suspend fun getServiceAppointment(appointmentId: String): Either<String, ServiceAppointments> {
        val result: FiberServiceResult<ServiceAppointments> =
            appointmentService.getServiceAppointments(appointmentId)
        return result.mapLeft { it.message?.message.toString() }
    }
}