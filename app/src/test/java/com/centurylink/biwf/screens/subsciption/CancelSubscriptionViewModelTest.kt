package com.centurylink.biwf.screens.subsciption

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.repos.CancelSubscriptionRepository
import com.centurylink.biwf.screens.subscription.CancelSubscriptionViewModel
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.mockito.MockitoAnnotations

class CancelSubscriptionViewModelTest: ViewModelBaseTest() {

    @MockK
    lateinit var cancelSubscriptionRepository: CancelSubscriptionRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: CancelSubscriptionViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }
}