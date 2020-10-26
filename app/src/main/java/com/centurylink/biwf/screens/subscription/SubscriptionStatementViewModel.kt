package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionStatementViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val zuoraPaymentRepository: ZuoraPaymentRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    val statementDetailsInfo: Flow<UiStatementDetails> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    var uiStatementDetails = UiStatementDetails()
    var invoicedId: String? = null
    var processedDate: String? = null

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SUBSCRIPTION_STATEMENT)
        progressViewFlow.latestValue = true
        initAPiCalls()
    }

    fun setInvoiceDetails(invoiced: String?, process: String?) {
        invoicedId = invoiced
        processedDate = process
    }

    fun initAPiCalls() {
        viewModelScope.launch {
            requestUserDetails()
            requestAccountDetails()
            requestPaymentInformation()
        }
    }

    fun logBackPress() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_PREVIOUS_STATEMENT)
    }

    fun logDonePress() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_PREVIOUS_STATEMENT)
    }

    private suspend fun requestUserDetails() {
        val userDetails = userRepository.getUserDetails()
        userDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_USER_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_USER_DETAILS_SUCCESS)
            uiStatementDetails = uiStatementDetails.copy(email = it.email)
        }
    }

    private suspend fun requestAccountDetails() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_SUCCESS)
            uiStatementDetails = uiStatementDetails.copy(
                billingAddress = formatBillingAddress(it) ?: ""
            )
        }
    }

    private suspend fun requestPaymentInformation() {
        val paymentDetails = zuoraPaymentRepository.getPaymentInformation(invoicedId!!)
        paymentDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_PAYMENT_INFO_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_PAYMENT_INFO_SUCCESS)
            // QA Environment comes with $ value
            val planCost: Double = it.planCostWithoutTax?.replace("$", "")?.toDouble() ?: 0.0
            val salesTaxCost: Double = it.salesTaxAmount?.replace("$", "")?.toDouble() ?: 0.0
            val totalCost: Double = planCost + salesTaxCost
            uiStatementDetails = uiStatementDetails.copy(
                paymentMethod = it.zuoraPaymentMethod ?: "",
                planName = it.productPlanNameC,
                successfullyProcessed = DateUtils.formatInvoiceDate(processedDate!!),
                planCost = String.format("%.2f", planCost),
                salesTaxCost = String.format("%.2f", salesTaxCost),
                totalCost = String.format("%.2f", totalCost)
            )
            statementDetailsInfo.latestValue = uiStatementDetails
            progressViewFlow.latestValue = false
        }
    }

    private fun formatBillingAddress(accountDetails: AccountDetails): String? {
        val formattedServiceAddressLine1 = accountDetails.billingAddress?.run {
            val billingAddressList = listOf(street, city)
            billingAddressList.filterNotNull().joinToString(separator = " ")
        }
        val formattedServiceAddressLine2 = accountDetails.billingAddress?.run {
            val billingAddressList = listOf(state, postalCode)
            billingAddressList.filterNotNull().joinToString(separator = " ")
        }
        return "$formattedServiceAddressLine1, $formattedServiceAddressLine2"
    }

    data class UiStatementDetails(
        val successfullyProcessed: String? = null,
        val paymentMethod: String? = null,
        val email: String? = null,
        val planName: String? = null,
        val planCost: String? = null,
        val salesTaxCost: String? = null,
        val promoCode: String? = null,
        val totalCost: String? = null,
        val billingAddress: String? = null
    )
}
