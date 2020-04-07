package com.centurylink.biwf.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.network.Resource
import com.centurylink.biwf.network.Status

import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.screens.notification.NotificationViewModel
import com.centurylink.biwf.testutils.event
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations


class NotificationViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: NotificationViewModel

    @MockK
    lateinit var notificationRepository: NotificationRepository

    val result = MediatorLiveData<Resource<NotificationSource>>()

    private val notifcationList = mutableListOf(
        Notification(
            NotificationActivity.KEY_UNREAD_HEADER, "",
            "", "", true, ""),
        Notification("1", "", "", "", true, ""),
        Notification("2", "", "", "", false, "")
    )

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        var notificationSource: NotificationSource = NotificationSource()
        notificationSource.notificationlist = notifcationList;
        result.value = Resource(Status.SUCCESS,notificationSource,"");
        every {(notificationRepository.getNotificationDetails())}.returns(result)
        viewModel = NotificationViewModel(notificationRepository)
    }

    @Test
    fun  onNotificationSuccess(){
        var data : LiveData<Resource<NotificationSource>> = viewModel.getNotificationDetails()
        data.value!!.status shouldEqual(Status.SUCCESS)
    }

    @Test
    fun ondisplayingClearAllDialog(){
        viewModel.displayClearAllDialogs()
        viewModel.displayClearAllEvent.event() shouldEqual(Unit)
    }

    @Test
    fun displayErrorDialogonServerError(){
        viewModel.displayErrorDialog()
        viewModel.errorEvents.event()shouldEqual ("Server error!Try again later")
    }
}


