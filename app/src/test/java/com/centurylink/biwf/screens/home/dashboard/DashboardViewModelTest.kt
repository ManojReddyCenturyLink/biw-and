package com.centurylink.biwf.screens.home.dashboard

import androidx.lifecycle.MediatorLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.network.Status
import com.centurylink.biwf.repos.CurrentAppointmentRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class DashboardViewModelTest : ViewModelBaseTest() {

    @MockK
    private lateinit var mockCurrentAppointmentRepository: CurrentAppointmentRepository

    @MockK
    lateinit var notificationRepository: NotificationRepository
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
    val result = MediatorLiveData<Resource<NotificationSource>>()
    private lateinit var viewModel: DashboardViewModel


    @Before
    fun setup() {
        every { mockCurrentAppointmentRepository.getCurrentAppointment("123") } returns true.toString()

        MockitoAnnotations.initMocks(this)
        var notificationSource: NotificationSource = NotificationSource()
        notificationSource.notificationlist = notificationList;
        result.value = Resource(Status.SUCCESS,notificationSource,"");
        every {(notificationRepository.getNotificationDetails())}.returns(result)
        viewModel = DashboardViewModel(currentAppointmentRepository = mockCurrentAppointmentRepository, notificationRepository = notificationRepository)

    }

    @Test
    fun onChangeAppointmentClicked_navigateToChangeAppointmentScreen() {
        viewModel.getChangeAppointment()
        Assert.assertEquals(
            "Change Appointment Screen wasn't Launched",
            DashboardCoordinatorDestinations.CHANGE_APPOINTMENT,
            viewModel.myState.value
        )
    }

    @Test
    fun `retrieve Notification with ViewModel and Repository returns an data`(){
        with(viewModel){
            getNotificationDetails()
            notificationLiveData.value = notificationList
        }
        Assert.assertTrue(viewModel.notificationLiveData.value?.size==notificationList.size)
    }

    @Test
    fun onNotificationClicked_navigateToNotificationDetailsScreen() {
        viewModel.navigateToNotificationDetails(notificationList.get(0))
        Assert.assertEquals(
            "Notification Details Screen wasn't Launched",
            DashboardCoordinatorDestinations.NOTIFICATION_DETAILS,
            viewModel.myState.value
        )
    }

}