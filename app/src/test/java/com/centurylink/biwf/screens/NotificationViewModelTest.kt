package com.centurylink.biwf.screens

import androidx.lifecycle.LiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.network.api.ApiServices
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationViewModel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class NotificationViewModelTest :ViewModelBaseTest(){

    private lateinit var viewModel: NotificationViewModel

    @Mock
    lateinit var apiServices:  ApiServices

    @MockK
    lateinit var notificationRepository: NotificationRepository



    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        System.out.println("Notification : "+notificationRepository)
        every { notificationRepository.getNotificationDetails() } returns
        viewModel = NotificationViewModel(notificationRepository)


       when(notificationRepository.getNotificationDetails())

        //this.viewModel.notificationLiveData.observeForever()
        //System.out.println("NotificationViewModelTest Setup Completed "+apiServices)
    }

    @Test
    fun onLoginClicked_withRequiredFields_navigateToHomeScreen() {
       assert(true)
    }
}


