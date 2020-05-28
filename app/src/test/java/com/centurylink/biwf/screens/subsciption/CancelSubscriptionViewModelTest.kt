package com.centurylink.biwf.screens.subsciption

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.repos.ZouraSubscriptionRepository
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionViewModel
import com.centurylink.biwf.testutils.event
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.*

class CancelSubscriptionViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var zuoraSubscriptionRepo: ZouraSubscriptionRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: CancelSubscriptionViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = CancelSubscriptionViewModel(zuoraSubscriptionRepo)
    }

    @Test
    fun onGetValidityDateSuccess() {
        //Need to Address this later

    }
}