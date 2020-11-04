package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.*
import com.centurylink.biwf.service.network.AppointmentService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import org.threeten.bp.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AppointmentRepository class - This class interacts with Appointment API Services. This Repository class
 * gets the data from the network. It handles all the Appointment related information from the Salesforce
 * backend  and the View models can consume the Appointment related information and display in the Activity
 * or Fragments.
 * @property preferences Instance for storing the value in shared preferences.
 * @property appointmentService Instance for interacting with the Sales force Appointment API.
 * @property integrationRestServices Instance for interacting with the Sales force Appointment API.
 * @constructor creates Appointment repository
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val preferences: Preferences,
    private val appointmentService: AppointmentService,
    private val integrationRestServices: IntegrationRestServices
) {

    /**
     * This method is used to get the Account Id that is stored in the  Shared Preferences
     *
     * @return The Account Id of the user.
     */
    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    /**
     * The Suspend function used for the purpose of fetching the AppointmentInformation from the
     * Salesforce backend
     *
     * @return AppointmentRecordsInfo if the API is success it returns the AppointmentRecordsInfo instance
     * Error in String format in case of API failure.
     */
    suspend fun getAppointmentInfo(): Either<String, AppointmentRecordsInfo> {
        val accountId = getAccountId()
        if (accountId.isNullOrEmpty()) {
            return Either.Left("Account ID is not available")
        }
        val finalQuery = String.format(EnvironmentPath.APPOINTMENT_INFO_QUERY, accountId)
        val result: FiberServiceResult<Appointments> =
            appointmentService.getAppointmentDetails(finalQuery)
        // val result: FiberServiceResult<Appointments> =
        // integrationRestServices.getAppointmentDetails("appointmentDetails")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val appointmentRecords = it.records?.elementAtOrElse(it.records?.lastIndex) { null }
            appointmentRecords?.let { it ->
                val serviceRecords = it.serviceResources?.records?.elementAtOrElse(0) { null }
                var timeZoneInfo = it.serviceTerritory?.operatingHours?.timeZone
                if (timeZoneInfo.isNullOrEmpty()) {
                    // TODO: currently for few account getting timezone as null, need to check
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
                        serviceLatitude = it.latitude ?: "0.0",
                        serviceLongitude = it.longitude ?: "0.0",
                        appointmentId = it.id,
                        timeZone = timeZoneInfo,
                        appointmentNumber = it.appointmentNumber
                    )
                    Either.Right(uiAppointmentRecords)
                }
            } ?: Either.Left("No Appointment Records")
        }
    }

    /**
     * The Suspend function used for the purpose of fetching the Appointment Slots available from the
     * Salesforce backend.
     *
     * @return AppointmentSlots if the API is success it returns the AppointmentSlots instance
     * Error in String format in case of API failure.
     */
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

    /**
     * The Suspend function used for the making a change in Appointment Scheduled by the User.
     *
     * @return RescheduleInfo if the API is success it returns the RescheduleInfo instance
     * Error in String format in case of API failure.
     */
    suspend fun modifyAppointmentInfo(rescheduleInfo: RescheduleInfo): Either<String, AppointmentResponse> {
        // val result: FiberServiceResult<AppointmentResponse> =
        //  integrationRestServices.submitAppointments(rescheduleInfo)
        val result: FiberServiceResult<AppointmentResponse> =
            appointmentService.reScheduleAppointment(rescheduleInfo)
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     * The Suspend function used for canceling the Appointment  Appointment Scheduled by the User.
     *
     * @param cancelAppointmentInfo The cancelAppoinment data to send to the server.
     * @return CancelResponse if the API is success it returns the CancelResponse instance
     * Error in String format in case of API failure.
     */
    suspend fun cancelAppointment(cancelAppointmentInfo: CancelAppointmentInfo): Either<String?, CancelResponse> {
        // val result: FiberServiceResult<AppointmentResponse> =
        //  integrationRestServices.submitAppointments(rescheduleInfo)
        val result: FiberServiceResult<CancelResponse> =
            appointmentService.cancelAppointment(cancelAppointmentInfo)

        return result.mapLeft {
            if (it.message?.message.toString()
                    .contains("System.JSONException")
            ) "Exception in Canceling Appointment" else it.message?.message
        }
    }
}
