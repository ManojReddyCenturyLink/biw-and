package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.billing.BillingDetails
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
            getUserDetails()
            getAccountInformation()
            getPaymentInformation()
        }
    }

    private suspend fun getUserDetails() {
        val userDetails = userRepository.getUserDetails()
        userDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            uiStatementDetails = uiStatementDetails.copy(email = it.email)
        }
    }

    private suspend fun getAccountInformation() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            uiStatementDetails = uiStatementDetails.copy(
                paymentMethod = it.paymentMethodName ?: "Visa ******* 2453",
                billingAddress = formatBillingAddress(it) ?:formatServiceAddress(it)
            )
        }
    }

    private suspend fun getPaymentInformation() {
        val paymentDetails = zuoraPaymentRepository.getPaymentInformation(invoicedId!!)
        paymentDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            uiStatementDetails = uiStatementDetails.copy(
                planName = it.productPlanNameC ?: "Fiber Internet",
                successfullyProcessed = DateUtils.formatInvoiceDate(processedDate!!),
                planCost = it.planCostWithoutTax ?: "100.0",
                salesTaxCost = it.salesTaxAmount ?: "0.0",
                totalCost = it.planCostWithoutTax ?: "100.0"
            )
            statementDetailsInfo.latestValue = uiStatementDetails
        }
    }

    private suspend fun getBillingInformation() {
        val billingDetailList = billingRepository.getBillingDetails()
        billingDetailList.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            statementDetailsInfo.latestValue = toUIStatementInfo(it[0])
        }
    }

    private fun formatBillingAddress(accountDetails: AccountDetails): String? {
        return accountDetails.billingAddress!!.run {
            val billingAddressList: MutableList<String> = mutableListOf<String>()
            billingAddressList.add(street!!)
            billingAddressList.add(city!!)
            billingAddressList.add(state!!)
            billingAddressList.add(postalCode!!)
            return@run billingAddressList.filterNotNull().joinToString(separator = ",")
        }
    }

    private fun formatServiceAddress(accountDetails: AccountDetails): String? {
        return accountDetails.run {
            val billingAddressList: MutableList<String> = mutableListOf<String>()
            billingAddressList.add(serviceStreet!!)
            billingAddressList.add(serviceCity!!)
            billingAddressList.add(serviceStateProvince!!)
            billingAddressList.add(servicePostalCode!!)
            return@run billingAddressList.filterNotNull().joinToString(separator = ",")
        }
    }

    private fun toUIStatementInfo(billDetails: BillingDetails): UiStatementDetails {
        return UiStatementDetails(
            successfullyProcessed = DateUtils.formatInvoiceDate(billDetails.ZuoraCreatedDate),
            paymentMethod = billDetails.zuoraPaymentMethod,
            planCost = billDetails.ZAmountWithoutTax,
            salesTaxCost = billDetails.ZtaxAmount,
            promoCode = billDetails.zuoraPaymentMethod,
            totalCost = billDetails.ZuoraAmountc
        )
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