package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.BillingRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionStatementViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val billingRepository: BillingRepository,
    private val userRepository: UserRepository,
    private val zuoraPaymentRepository: ZuoraPaymentRepository
) : BaseViewModel() {

    val statementDetailsInfo: Flow<UiStatementDetails> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    var uiStatementDetails = UiStatementDetails()
    var invoicedId: String? = null
    var processedDate: String? = null

    init {
        initAPiCalls()
    }

    fun setInvoiceDetails(invoiced: String?, process: String?) {
        invoicedId = invoiced
        processedDate = process
    }

    private fun initAPiCalls() {
        viewModelScope.launch {
            requestUserDetails()
            requestAccountDetails()
            requestPaymentInformation()
        }
    }

    private suspend fun requestUserDetails() {
        val userDetails = userRepository.getUserDetails()
        userDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            uiStatementDetails = uiStatementDetails.copy(email = it.email)
        }
    }

    private suspend fun requestAccountDetails() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            uiStatementDetails = uiStatementDetails.copy(
                paymentMethod = it.paymentMethodName ?: "Visa ******* 2453",
                billingAddress = formatBillingAddress(it) ?: ""
            )
        }
    }

    private suspend fun requestPaymentInformation() {
        val paymentDetails = zuoraPaymentRepository.getPaymentInformation(invoicedId!!)
        paymentDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            // QA Environment comes with $ value
            val planCost: Double = it.planCostWithoutTax?.replace("$", "")?.toDouble() ?: 0.0
            val salesTaxCost: Double = it.salesTaxAmount?.replace("$", "")?.toDouble() ?: 0.0
            val totalCost: Double = planCost + salesTaxCost
            uiStatementDetails = uiStatementDetails.copy(
                planName = it.productPlanNameC ?: "Fiber Internet",
                successfullyProcessed = DateUtils.formatInvoiceDate(processedDate!!),
                planCost = String.format("%.1f", planCost),
                salesTaxCost = String.format("%.1f", salesTaxCost),
                totalCost = String.format("%.1f", totalCost)
            )
            statementDetailsInfo.latestValue = uiStatementDetails
        }
    }

    private fun formatBillingAddress(accountDetails: AccountDetails): String? {
        return accountDetails.billingAddress?.run {
            val billingAddressList = listOf(street, city, state, postalCode, country)
            return billingAddressList.filterNotNull().joinToString(separator = ", ")
        }
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