package com.centurylink.biwf.screens.subsciption

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.SubscriptionCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.Attributes
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.account.RecordsItem
import com.centurylink.biwf.model.subscriptionDetails.SubscriptionDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ZouraSubscriptionRepository
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
import org.junit.Test

class SubscriptionViewModelTest : ViewModelBaseTest() {

    @MockK(relaxed = true)
    private lateinit var zuoraPaymentRepository: ZuoraPaymentRepository

    @MockK(relaxed = true)
    private lateinit var accountRepository: AccountRepository

    @MockK(relaxed = true)
    private lateinit var zouraSubscriptionRepository: ZouraSubscriptionRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    private lateinit var accountDetails: AccountDetails

    private lateinit var viewModel: SubscriptionViewModel

    private lateinit var paymentList: PaymentList

    private lateinit var subscriptionDetails: SubscriptionDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        val jsonPaymentListString = readJson("zuorapayment.json")
        paymentList = fromJson(jsonPaymentListString)
        val jsonSubscriptionDetails = readJson("zuorasubscriptiondetails.json")
        subscriptionDetails = fromJson(jsonSubscriptionDetails)
        run { analyticsManagerInterface }
        coEvery { zuoraPaymentRepository.getInvoicesList() } returns Either.Right(paymentList)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        coEvery { zouraSubscriptionRepository.getSubscriptionDetails() } returns Either.Right(subscriptionDetails)
        viewModel = SubscriptionViewModel(
            zuoraPaymentRepository = zuoraPaymentRepository,
            accountRepository = accountRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface,
            zouraSubscriptionRepository = zouraSubscriptionRepository
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

    @Test
    fun testrequestSubscriptionDetailsFailure() {
        runBlockingTest {
            launch {
                coEvery { accountRepository.getLiveCardDetails() } returns Either.Left("Error Response")
                coEvery { zouraSubscriptionRepository.getSubscriptionDetails() } returns Either.Left("Error Response")
                viewModel.initApis()
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(),
                    "Error Response"
                )
            }
        }
    }

    @Test
    fun testOnEditBillingContainerClicked() = runBlockingTest {
        launch {
            viewModel.onEditBillingContainerClicked()
        }

        Assert.assertEquals(
            "Edit Payment Info Screen wasn't launched",
            SubscriptionCoordinatorDestinations.EDIT_PAYMENT,
            viewModel.myState.first()
        )
    }

    @Test
    fun launchStatement() = runBlockingTest {
        launch {
            viewModel.launchStatement(
                RecordsItem(
                    createdDate = "",
                    zuoraInvoiceC = "",
                    attributes = Attributes(),
                    id = ""
                )
            )
            Assert.assertEquals(viewModel.myState.first().name, "STATEMENT")
        }
    }

    @Test
    fun launchManageSubscription() = runBlockingTest {
        launch {
            viewModel.launchManageSubscription()
            Assert.assertEquals(viewModel.myState.first().name, "MANAGE_MY_SUBSCRIPTION")
        }
    }

    @Test
    fun logDoneBtnClick() {
        Assert.assertNotNull(viewModel.logDoneBtnClick())
    }
}
