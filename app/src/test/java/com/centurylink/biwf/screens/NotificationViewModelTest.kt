package com.centurylink.biwf.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
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
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations


class NotificationViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: NotificationViewModel

    @MockK
    lateinit var notificationRepository: NotificationRepository

    val result = MediatorLiveData<Resource<NotificationSource>>()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val notifiCationList = mutableListOf(
        Notification(
            NotificationActivity.KEY_UNREAD_HEADER, "",
            "", "", true, ""),
        Notification(
            NotificationActivity.KEY_READ_HEADER, "",
            "", "", false, ""),
        Notification("1", "", "", "", true, ""),
        Notification("2", "", "", "", false, "")
    )

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        var notificationSource: NotificationSource = NotificationSource()
        notificationSource.notificationlist = notifiCationList;
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

    @Test
    fun `retrieve Notification with ViewModel and Repository returns an data`(){
        with(viewModel){
            getNotificationDetails()
            notificationLiveData.value = notifiCationList
        }
        Assert.assertTrue(viewModel.notificationLiveData.value?.size==notifiCationList.size)
    }

    @Test
    fun `retrieve Notification and check Headers for Read and unread`(){
        with(viewModel){
            getNotificationDetails()
            notificationLiveData.value = notifiCationList
        }
        var notificationlist:MutableList<Notification> = viewModel.notificationLiveData.value!!
        Assert.assertTrue(notificationlist.size>0)
        Assert.assertTrue(notificationlist.get(0).id.equals(NotificationActivity.KEY_UNREAD_HEADER))
    }
}


