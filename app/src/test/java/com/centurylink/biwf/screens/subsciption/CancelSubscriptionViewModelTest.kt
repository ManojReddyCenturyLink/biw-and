package com.centurylink.biwf.screens.subsciption

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.repos.CancelSubscriptionRepository
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
    lateinit var cancelSubscriptionRepository: CancelSubscriptionRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: CancelSubscriptionViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockDate()
        every { (cancelSubscriptionRepository.getSubscriptionValidity()) }.returns(mockDate())
        viewModel = CancelSubscriptionViewModel(cancelSubscriptionRepository)
    }

    @Test
    fun onGetValidityDateSuccess() {
        with(viewModel) {
            viewModel.getCancellationValidity()
            viewModel.cancelSubscriptionDate.event()?.equals(mockDate())
        }
        val d1 = viewModel.cancelSubscriptionDate.event()
        assertEqualDates(d1!!, mockDate())
    }

    private fun mockDate(): Date {
        Calendar.getInstance().apply {
            add(Calendar.DATE, 7)
            return time
        }
    }

    private fun assertEqualDates(date1: Date, date2: Date): Boolean {
        val formatter = SimpleDateFormat("dd MMM yyyy")
        val d1: String = formatter.format(date1)
        val d2: String = formatter.format(date2)
        return d1 == d2
    }
}