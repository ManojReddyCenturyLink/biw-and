package com.centurylink.biwf.screens.home.dashboard

import androidx.lifecycle.MediatorLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.network.Status
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

@Suppress("EXPERIMENTAL_API_USAGE")
class DashboardViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var notificationRepository: NotificationRepository
    @MockK
    lateinit var appointmentRepository: AppointmentRepository


    private val notificationList = mutableListOf(
        Notification(
            NotificationActivity.KEY_UNREAD_HEADER, "",
            "", "", true, ""),
        Notification(
            NotificationActivity.KEY_READ_HEADER, "",
            "", "", false, ""),
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
        viewModel = DashboardViewModel(
            notificationRepository = notificationRepository,
            appointmentRepository = appointmentRepository
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
    fun `retrieve Notification with ViewModel and Repository returns an data`(){
        with(viewModel){
            notificationLiveData.value = notificationList
        }
        Assert.assertTrue(viewModel.notificationLiveData.value?.size==notificationList.size)
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