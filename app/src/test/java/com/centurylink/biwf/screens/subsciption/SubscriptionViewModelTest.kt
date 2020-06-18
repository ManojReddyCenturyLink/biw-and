package com.centurylink.biwf.screens.subsciption

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.screens.subscription.SubscriptionViewModel
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
            accountRepository = accountRepository
        )
    }

    @Test
    fun testAccountandPayments() {
        runBlockingTest {
            launch {
                viewModel.initApis()
                val invoiceData = viewModel.invoicesListResponse.latestValue
                val uiFlowable = viewModel.uiFlowable.latestValue
                Assert.assertEquals(invoiceData.records[0].zuoraInvoiceC, "a1if0000002aKSbAAM")
                Assert.assertEquals(uiFlowable.cvv, "123")
                Assert.assertEquals(uiFlowable.creditCardNumber, "1234 - 1234 - 1234 - 1234")
            }
        }
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