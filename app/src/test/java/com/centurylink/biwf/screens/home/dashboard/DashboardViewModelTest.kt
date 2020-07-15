package com.centurylink.biwf.screens.home.dashboard

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.threeten.bp.LocalDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class DashboardViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var notificationRepository: NotificationRepository
    @MockK
    lateinit var appointmentRepository: AppointmentRepository
    @MockK
    private lateinit var mockPreferences: Preferences
    @MockK
    private lateinit var mockAssiaRepository: AssiaRepository

    private val notificationList = mutableListOf(
        Notification(
            NotificationActivity.KEY_UNREAD_HEADER, "",
            "", "", true, ""
        ),
        Notification(
            NotificationActivity.KEY_READ_HEADER, "",
            "", "", false, ""
        ),
        Notification("1", "", "", "", true, ""),
        Notification("2", "", "", "", false, "")
    )

    private lateinit var viewModel: DashboardViewModel


    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val notificationSource = NotificationSource()
        notificationSource.notificationlist = notificationList
       // result.value = Resource(Status.SUCCESS, notificationSource, "")
        val date: LocalDateTime = LocalDateTime.now()
        every { mockPreferences.getUserType() } returns true
        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Right(
            AppointmentRecordsInfo(
                serviceAppointmentStartDate = date,
                serviceAppointmentEndTime = date,
                serviceEngineerName = "",
                serviceEngineerProfilePic = "",
                serviceStatus = ServiceStatus.COMPLETED,
                serviceLatitude = "",
                serviceLongitude = "",
                jobType = "",
                appointmentId = "",
                timeZone = ""
            )
        )
        coEvery { notificationRepository.getNotificationDetails() } returns Either.Right(
            NotificationSource()
        )
        viewModel = DashboardViewModel(
            notificationRepository = notificationRepository,
            appointmentRepository = appointmentRepository,
            sharedPreferences = mockPreferences,
            assiaRepository = mockAssiaRepository,
            modemRebootMonitorService = mockModemRebootMonitorService
        )
        // Need to Revisit Test cases
    }
}