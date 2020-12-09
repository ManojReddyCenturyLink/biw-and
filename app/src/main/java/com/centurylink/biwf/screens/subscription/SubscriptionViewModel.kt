package com.centurylink.biwf.screens.subscription

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SubscriptionCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.BillingAddress
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.account.RecordsItem
import com.centurylink.biwf.model.subscriptionDetails.Records
import com.centurylink.biwf.model.subscriptionDetails.SubscriptionDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ZouraSubscriptionRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val zuoraPaymentRepository: ZuoraPaymentRepository,
    private val accountRepository: AccountRepository,
    private val zouraSubscriptionRepository: ZouraSubscriptionRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    val myState = EventFlow<SubscriptionCoordinatorDestinations>()
    private lateinit var userAccount: AccountDetails
    private lateinit var subscriptionDetails: SubscriptionDetails

    private var serviceAddressData: BillingAddress = BillingAddress()
    private var billingAddress: BillingAddress = BillingAddress()

    val planName: Flow<String> = BehaviorStateFlow()
    val planDetails: Flow<String> = BehaviorStateFlow()
    val invoicesListResponse: Flow<PaymentList> = BehaviorStateFlow()
    var progressViewFlow = EventFlow<Boolean>()
    var errorMessageFlow = EventFlow<String>()
    var paymentmethod: Flow<String> = BehaviorStateFlow()
    var subscriptionDetailsRecord: Flow<Records> = BehaviorStateFlow()

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SUBSCRIPTION)
        initApis()
    }

    fun initApis() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestAccountDetails()
            requestInvoiceList()
            requestCardInfo()
            requestSubscriptionDetails()
        }
    }

    private suspend fun requestSubscriptionDetails() {
        val userAccountDetails = zouraSubscriptionRepository.getSubscriptionDetails()
        userAccountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            progressViewFlow.latestValue = false
            subscriptionDetails = it
            processSubscriptionData()
        }
    }

    private fun processSubscriptionData() {
        if (subscriptionDetails.records.isNotEmpty()) {
            subscriptionDetailsRecord.latestValue = subscriptionDetails.records[0]
        }
    }

    private suspend fun requestAccountDetails() {
        val userAccountDetails = accountRepository.getAccountDetails()
        userAccountDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_SUCCESS)
            userAccount = it
            processAccountData()
        }
    }

    private fun processAccountData() {
        if (userAccount.billingAddress != null) {
            billingAddress = userAccount.billingAddress!!
            serviceAddressData = BillingAddress(
                street = userAccount.serviceStreet ?: billingAddress.street,
                city = userAccount.serviceCity ?: billingAddress.city,
                state = userAccount.serviceStateProvince ?: billingAddress.state,
                postalCode = userAccount.servicePostalCode ?: billingAddress.postalCode
            )
        }
        planName.latestValue = userAccount.productNameC ?: ""
        planDetails.latestValue = userAccount.productPlanNameC ?: ""
    }

    fun onEditBillingContainerClicked() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_EDIT_BILLING_INFO)
        myState.latestValue = SubscriptionCoordinatorDestinations.EDIT_PAYMENT
    }

    fun launchStatement(item: RecordsItem) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_PREVIOUS_STATEMENT)
        val bundle = Bundle()
        bundle.putString(
            SubscriptionStatementActivity.SUBSCRIPTION_STATEMENT_INVOICE_ID,
            item.id
        )
        bundle.putString(
            SubscriptionStatementActivity.SUBSCRIPTION_STATEMENT_DATE,
            item.createdDate
        )
        SubscriptionCoordinatorDestinations.bundle = bundle
        myState.latestValue = SubscriptionCoordinatorDestinations.STATEMENT
    }

    fun launchManageSubscription() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_MANAGE_SUBSCRIPTION)
        myState.latestValue = SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION
    }

    fun logDoneBtnClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_SUBSCRIPTION_SCREEN)
    }

    private suspend fun requestInvoiceList() {
        val paymentList = zuoraPaymentRepository.getInvoicesList()
        paymentList.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_INVOICES_LIST_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_INVOICES_LIST_SUCCESS)
            invoicesListResponse.latestValue = it
        }
    }

    private suspend fun requestCardInfo() {
        val cardInfoResponse = accountRepository.getLiveCardDetails()
        cardInfoResponse.fold(
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_LIVE_CARD_INFO_FAILURE)
                errorMessageFlow.latestValue = it
            }
        ) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_LIVE_CARD_INFO_SUCCESS)
            if (it.isDone) {
                val creditCardInfo = it.list.firstOrNull()?.creditCardSummary
                if (!creditCardInfo.isNullOrEmpty()) {
                    paymentmethod.latestValue = creditCardInfo
                }
            }
        }
    }
}
