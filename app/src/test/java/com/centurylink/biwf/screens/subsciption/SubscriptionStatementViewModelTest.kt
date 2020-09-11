package com.centurylink.biwf.screens.subsciption

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.payment.PaymentDetails
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.screens.subscription.SubscriptionStatementViewModel
import com.centurylink.biwf.service.network.ZuoraPaymentService
import com.centurylink.biwf.utility.Constants
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

class SubscriptionStatementViewModelTest : ViewModelBaseTest() {

    @MockK(relaxed = true)
    private lateinit var accountRepository: AccountRepository

    @MockK(relaxed = true)
    private lateinit var userRepository: UserRepository

    @MockK(relaxed = true)
    private lateinit var zuoraPaymentRepository: ZuoraPaymentRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @MockK(relaxed = true)
    private lateinit var zuoraPaymentService: ZuoraPaymentService

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: SubscriptionStatementViewModel

    private lateinit var accountDetails: AccountDetails

    private lateinit var userDetails: UserDetails

    private lateinit var paymentDetails: PaymentDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        val userDetailsString = readJson("userdetails.json")
        userDetails = fromJson(userDetailsString)
        val jsonPaymentDetailsString = readJson("zuorastatement.json")
        paymentDetails = fromJson(jsonPaymentDetailsString)
        run { analyticsManagerInterface }
        coEvery { userRepository.getUserDetails() } returns Either.Right(userDetails)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        viewModel = SubscriptionStatementViewModel(
            userRepository = userRepository,
            accountRepository = accountRepository,
            zuoraPaymentRepository = zuoraPaymentRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testStatementFunctionsSuccess() {
        runBlockingTest {
            launch {
                coEvery { zuoraPaymentRepository.getPaymentInformation(any()) } returns Either.Right(
                    paymentDetails
                )
                viewModel.setInvoiceDetails("12345", "2020-05-14T07:27:04.000+0000")
                viewModel.initAPiCalls()
                val uiStatementDetails = viewModel.statementDetailsInfo.latestValue
                println(uiStatementDetails)
                Assert.assertEquals(uiStatementDetails.email, "pravin.kumar@accenture.com")
                Assert.assertEquals(uiStatementDetails.planName, "Fiber Gigabit")
                Assert.assertEquals(uiStatementDetails.planCost, "65.00")
                Assert.assertEquals(uiStatementDetails.successfullyProcessed, "05/14/20")
                Assert.assertEquals(uiStatementDetails.promoCode, null)
                Assert.assertEquals(uiStatementDetails.paymentMethod, null)
                Assert.assertEquals(uiStatementDetails.salesTaxCost, "0.00")
                Assert.assertEquals(uiStatementDetails.totalCost, "65.00")
                Assert.assertEquals(
                    uiStatementDetails.billingAddress,
                    "8581 Emerson Court, Denver, Colorado, 80229, United States"
                )
            }
        }
    }

    @Test
    fun testRequestUserDetailsFailure() {
        runBlockingTest {
            launch {
                coEvery { userRepository.getUserDetails() } returns Either.Left("Error in User")
                viewModel.initAPiCalls()
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(),
                    "Error in User"
                )
            }
        }
    }

    @Test
    fun testRequestAccountDetailsFailure() {
        runBlockingTest {
            launch {
                coEvery { accountRepository.getAccountDetails() } returns Either.Left("Error in Account")
                viewModel.initAPiCalls()
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(),
                    "Error in Account"
                )
            }
        }
    }


    @Test
    fun testRequestBillingAddressNull() {
        runBlockingTest {
            launch {
                val emptyString: String? = null
                val accountInfo = AccountDetails(
                    paymentMethodName = "Visa",
                    cellPhoneOptInC = false,
                    emailAddress = "",
                    emailOptInC = false,
                    isBillingAddressUpdated = false,
                    marketingOptInC = "",
                    productPlanNameC = emptyString
                )
                coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountInfo)
                viewModel.initAPiCalls()
                assert(true)
            }
        }
    }

    @Test
    fun testSetInvoiceDetails() {
        runBlockingTest {
            launch {
                viewModel.setInvoiceDetails("1", "09/09/2020")
                Assert.assertEquals(viewModel.invoicedId, "1")
                Assert.assertEquals(viewModel.processedDate, "09/09/2020")
            }
        }
    }

    @Test
    fun testLogBackPress() {
        Assert.assertNotNull(viewModel.logBackPress())
    }

    @Test
    fun testLogDonePress() {
        Assert.assertNotNull(viewModel.logDonePress())
    }


    @Test
    fun testRequestPaymentInformationSuccess() {
        runBlockingTest {
            launch {
                coEvery { zuoraPaymentRepository.getPaymentInformation(any()) } returns Either.Right(
                    paymentDetails
                )
                viewModel.setInvoiceDetails("12345", "2020-05-14T07:27:04.000+0000")
                viewModel.initAPiCalls()

                Assert.assertEquals(
                    viewModel.statementDetailsInfo.first().planName,
                    "Fiber Gigabit"
                )
            }
        }
    }

    @Test
    fun testRequestPaymentInformationError() {
        runBlockingTest {
            launch {
                coEvery { zuoraPaymentRepository.getPaymentInformation(any()) } returns Either.Left(
                    Constants.ERROR
                )
                viewModel.setInvoiceDetails("12345", "2020-05-14T07:27:04.000+0000")
                viewModel.initAPiCalls()

                Assert.assertEquals(viewModel.errorMessageFlow.first(), Constants.ERROR)
            }
        }
    }
}