package com.centurylink.biwf.screens.support.schedulecallback

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.ContactInfoCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactInfoViewModelTest: ViewModelBaseTest() {

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @MockK
    private lateinit var modemRebootMonitorService: ModemRebootMonitorService

    @MockK
    private lateinit var viewModel: ContactInfoViewModel

    @MockK
    private lateinit var accountRepository: AccountRepository
    private lateinit var accountDetails: AccountDetails
    private lateinit var uiAccountDetails: ContactInfoViewModel.UiAccountDetails
    var error: MutableLiveData<Errors> = MutableLiveData()

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        viewModel = ContactInfoViewModel(
            modemRebootMonitorService = modemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface,
            accountRepository = accountRepository)
        uiAccountDetails = ContactInfoViewModel.UiAccountDetails(cellPhone = "")
    }

    @Test
    fun testInitApiCall() {
        runBlockingTest {
            launch {
                viewModel.initContactApiCall()
            }
        }
    }

    @Test
    fun testOnPhoneNumberEmpty() {
        viewModel.onPhoneNumberChanged("")
        error.value = viewModel.validateInput()
        MatcherAssert.assertThat("Phone Number Empty", error.value!!.contains("mobileNumberError"))
    }

    @Test
    fun testOnPhoneNumberLesserThanTenDigits() {
        viewModel.onPhoneNumberChanged("1234")
        error.value = viewModel.validateInput()
        MatcherAssert.assertThat("Phone Number Too Short", error.value!!.contains("mobileNumberError"))
    }

    @Test
    fun testNavigateToSelectTimeActivity() {
        runBlockingTest {
            launch {
                viewModel.launchSelectTime("", "", "")
            }
            assertEquals(
                "Select time Activity was Launched",
                ContactInfoCoordinatorDestinations.SELECT_TIME,
                viewModel.myState.first()
            )
        }
    }

    @Test
    fun testAnalyticsButtonClicked() {
        assertNotNull(analyticsManagerInterface)
        viewModel.logModemRebootErrorDialog()
        viewModel.logModemRebootSuccessDialog()
    }


}