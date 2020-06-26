package com.centurylink.biwf.screens.subsciption

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.screens.subscription.SubscriptionViewModel
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SubscriptionViewModelTest : ViewModelBaseTest() {

    @MockK(relaxed = true)
    private lateinit var zuoraPaymentRepository: ZuoraPaymentRepository

    @MockK(relaxed = true)
    private lateinit var accountRepository: AccountRepository

    @MockK(relaxed = true)
    private lateinit var preferences: Preferences

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var accountDetails: AccountDetails

    private lateinit var viewModel: SubscriptionViewModel

    private lateinit var paymentList: PaymentList

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        val jsonPaymentListString = readJson("zuorapayment.json")
        paymentList = fromJson(jsonPaymentListString)
        coEvery { zuoraPaymentRepository.getInvoicesList() } returns Either.Right(paymentList)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        viewModel = SubscriptionViewModel(
            zuoraPaymentRepository = zuoraPaymentRepository,
            accountRepository = accountRepository,
            preferences = preferences
        )
    }

    @Test
    fun testrequestInvoiceListFailure() {
        runBlockingTest {
            launch {
                coEvery { accountRepository.getAccountDetails() } returns Either.Left("Error in Account")
                viewModel.initApis()
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(),
                    "Error in Account"
                )
            }
        }
    }

    @Test
    fun testrequestInvoiceAccountFailure() {
        runBlockingTest {
            launch {
                coEvery { zuoraPaymentRepository.getInvoicesList() } returns Either.Left("Error in Payments")
                viewModel.initApis()
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(),
                    "Error in Payments"
                )
            }
        }
    }
}