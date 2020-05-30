package com.centurylink.biwf.screens.home.dashboard

import androidx.lifecycle.MediatorLiveData
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.network.Status
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.threeten.bp.LocalDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class DashboardViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var notificationRepository: NotificationRepository
    @MockK
    lateinit var appointmentRepository: AppointmentRepository
    @MockK
    private lateinit var mockPreferences: Preferences

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
    private val result = MediatorLiveData<Resource<NotificationSource>>()
    private lateinit var viewModel: DashboardViewModel


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val notificationSource = NotificationSource()
        notificationSource.notificationlist = notificationList
        result.value = Resource(Status.SUCCESS, notificationSource, "")
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
                appointmentId = ""
            )
        )
        coEvery { notificationRepository.getNotificationDetails() } returns Either.Right(
            NotificationSource()
        )
        viewModel = DashboardViewModel(
            notificationRepository = notificationRepository,
            appointmentRepository = appointmentRepository,
            sharedPreferences = mockPreferences
        )
    }

    @Test
    fun onChangeAppointmentClicked_navigateToChangeAppointmentScreen() = runBlockingTest {
        launch {
            viewModel.getChangeAppointment()
        }
        Assert.assertEquals(
            "Change Appointment Screen wasn't Launched",
            DashboardCoordinatorDestinations.CHANGE_APPOINTMENT,
            viewModel.myState.first()
        )
    }

    @Test
    fun `retrieve Notification with ViewModel and Repository returns an data`() {
        with(viewModel) {
            notifications.value = notificationList
        }
        Assert.assertTrue(viewModel.notifications.value.size == notificationList.size)
    }

    @Test
    fun onNotificationClicked_navigateToNotificationDetailsScreen() = runBlockingTest {
        launch {
            viewModel.navigateToNotificationDetails(notificationList[0])
        }

        Assert.assertEquals(
            "Notification Details Screen wasn't Launched",
            DashboardCoordinatorDestinations.NOTIFICATION_DETAILS,
            viewModel.myState.first()
        )
    }
}