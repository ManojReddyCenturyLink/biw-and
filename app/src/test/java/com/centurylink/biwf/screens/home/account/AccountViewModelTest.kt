package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.Account
import com.centurylink.biwf.model.CommunicationPreferences
import com.centurylink.biwf.model.Subscription
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.CommunicationRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.SubscriptionRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.internal.assertSame
import org.junit.Before
import org.junit.Test

class AccountViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: AccountViewModel
    private lateinit var mockAccount: Account
    private lateinit var mockSubscription: Subscription
    private lateinit var mockCommunicationPreferences: CommunicationPreferences

    @MockK
    private lateinit var mockCommunicationRepository: CommunicationRepository
    @MockK
    private lateinit var mockSubscriptionRepository: SubscriptionRepository
    @MockK
    private lateinit var mockAccountRepository: AccountRepository

    @MockK
    private lateinit var mockContactRepository: ContactRepository

    @Before
    fun setup() {
        setUpDummyAccount()
        setUpDummyPreferences()
        setUpDummySubscription()

        every { mockAccountRepository.login(any(), any(), any()) } returns true
        every { mockAccountRepository.getAccount() } returns MutableLiveData(mockAccount)

        every { mockCommunicationRepository.getPreferences() } returns MutableLiveData(
            mockCommunicationPreferences
        )

        every { mockSubscriptionRepository.getSubscription() } returns MutableLiveData(
            mockSubscription
        )

        viewModel = AccountViewModel(
            communicationRepository = mockCommunicationRepository,
            subscriptionRepository = mockSubscriptionRepository,
            accountRepository = mockAccountRepository,
            contactRepository = mockContactRepository
        )
    }

    @Test
    fun onBiometricSwitchChange_fromTrueToFalse() {
        assertSame(true, viewModel.biometricStatus.value)
        viewModel.onBiometricChange(false)
        assertSame(false, viewModel.biometricStatus.value)
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

    private fun setUpDummyAccount() {
        mockAccount = Account(
            fullName = "Tester Chester",
            streetAddress = "1234 Candycane Lane",
            city = "North Side",
            state = "WA",
            zipcode = "12125",
            cellNumber = "(111) 111-1111",
            homeNumber = "(111) 111-1111",
            workNumber = "(111) 111-1111",
            emailAddress = "testing@email.com"
        )
    }

    private fun setUpDummyPreferences() {
        mockCommunicationPreferences = CommunicationPreferences(
            biometricStatus = true,
            serviceCallsStatus = true,
            marketingEmailsStatus = true,
            marketingCallsAndTextStatus = true
        )
    }

    private fun setUpDummySubscription() {
        mockSubscription = Subscription(
            subscriptionName = "testing sub name",
            subscriptionDetails = "testing details",
            subscriptionDate = "01/01/20",
            subscriptionCardType = "Visa",
            subscriptionCardDigits = "1234"
        )
    }
}