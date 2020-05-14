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
    var invoicedId:String?=null
    init {
        getUserDetails()
    }

    fun setInvoiceId(invoiced:String){
        invoicedId =invoiced;
    }

    private fun getUserDetails() {
        viewModelScope.launch {
            val userDetails = userRepository.getUserDetails()
            userDetails.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {
                uiStatementDetails = uiStatementDetails.copy(email = it.email)
                getAccountInformation()
            }
        }
    }

    private fun getAccountInformation() {
        viewModelScope.launch {
            val accountDetails = accountRepository.getAccountDetails()
            accountDetails.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {
                uiStatementDetails = uiStatementDetails.copy(
                    planName = it.productPlanNameC,
                    billingAddress = formatBillingAddress(it)
                )
                getPaymentInformation()
            }

        }
    }

    private fun getPaymentInformation(){
        viewModelScope.launch {
            val paymentDetails = zuoraPaymentRepository.getPaymentInformation(invoicedId!!)
            paymentDetails.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {

                getBillingInformation()
            }

        }
    }

    private fun getBillingInformation() {
        viewModelScope.launch {
            val billingDetailList = billingRepository.getBillingDetails()
            billingDetailList.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {
                statementDetailsInfo.latestValue = toUIStatementInfo(it[0])
            }

        }
    }

    private fun formatBillingAddress(billDetails: AccountDetails): String {
        return billDetails.billingAddress!!.run {
            val billingAddressList: MutableList<String> = mutableListOf<String>()
            billingAddressList.add(street!!)
            billingAddressList.add(city!!)
            billingAddressList.add(state!!)
            billingAddressList.add(postalCode!!)
            billingAddressList.add(country!!)
            return@run billingAddressList.filterNotNull().joinToString(separator = " , ")
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