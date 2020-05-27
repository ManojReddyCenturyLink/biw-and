package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.AccountCoordinatorDestinations
import com.centurylink.biwf.model.Account
import com.centurylink.biwf.model.CommunicationPreferences
import com.centurylink.biwf.model.Subscription
import com.centurylink.biwf.repos.*
import com.centurylink.biwf.service.auth.AuthService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.internal.assertSame
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AccountViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: AccountViewModel

    @MockK
    private lateinit var mockuserRepository: UserRepository
    @MockK
    private lateinit var mockAccountRepository: AccountRepository

    @MockK
    private lateinit var mockContactRepository: ContactRepository
    @MockK
    private lateinit var authService: AuthService<*>

    @Before
    fun setup() {
        every { mockAccountRepository.login(any(), any(), any()) } returns true
        viewModel = AccountViewModel(
            accountRepository = mockAccountRepository,
            contactRepository = mockContactRepository,
            userRepository = mockuserRepository,
            authService = authService

        )
    }

    @Test
    fun onBiometricSwitchChange_fromTrueToFalse() {
        //Need to Revisit this
        /*assertSame(true, viewModel.accountDetailsInfo)
        viewModel.onBiometricChange(false)
        assertSame(false, viewModel.biometricStatus.value)*/
    }

    @Test
    fun onServiceCallsSwitchChange_fromTrueToFalse() {
        //Need To Revisit this
       /* assertSame(true, viewModel.serviceCallsAndTextStatus.value)
        viewModel.onServiceCallsAndTextsChange(false)
        assertSame(false, viewModel.serviceCallsAndTextStatus.value)*/
    }

    @Test
    fun onMarketingCallsSwitchChange_fromTrueToFalse() {
        //Need To Revisit this
        /*assertSame(true, viewModel.marketingCallsAndTextStatus.value)
        viewModel.onMarketingCallsAndTextsChange(false)
        assertSame(false, viewModel.marketingCallsAndTextStatus.value)*/
    }

    @Test
    fun onMarketingEmailsSwitchChange_fromTrueToFalse() {
        //Need To Revisit this
       /* assertSame(true, viewModel.marketingEmailStatus.value)
        viewModel.onMarketingEmailsChange(false)
        assertSame(false, viewModel.marketingEmailStatus.value)*/
    }

    @Test
    fun onPersonalInfoCardClick_navigateToPersonalInfoScreen() = runBlockingTest {
        launch {
            viewModel.onPersonalInfoCardClick()
        }

        Assert.assertEquals(
            "Personal Info Screen wasn't Launched",
            AccountCoordinatorDestinations.PROFILE_INFO,
            viewModel.myState.first()
        )
    }

}