package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.billing.BillingDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.BillingRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionStatementViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val billingRepository: BillingRepository
) : BaseViewModel() {

    val statementDetailsInfo: Flow<UiStatementDetails> = BehaviorStateFlow()

    init {
        getAccountInformation()
        getBillingInformation()
    }

    private fun getAccountInformation() {
        viewModelScope.launch {
            try {
                val accountDetails = accountRepository.getAccountDetails()
            } catch (e: Throwable) {
            }
        }
    }

    private fun getBillingInformation() {
        viewModelScope.launch {
            try {
                val billingDetailList = billingRepository.getBillingDetails()
                statementDetailsInfo.latestValue = toUIStatementInfo(billingDetailList[0])
            } catch (e: Throwable) {
            }
        }
    }

    private fun formatBillingAddress(billDetails: BillingDetails): String {
        return billDetails.billingAddress.run {
            val billingAddressList: MutableList<String> = mutableListOf<String>()
            billingAddressList.add(street)
            billingAddressList.add(city)
            billingAddressList.add(state)
            billingAddressList.add(postalCode)
            billingAddressList.add(country)
            return@run billingAddressList.filterNotNull().joinToString(separator = " , ")
        }
    }

    private fun toUIStatementInfo(billDetails: BillingDetails): UiStatementDetails {
        return UiStatementDetails(
            successfullyProcessed = DateUtils.formatInvoiceDate(billDetails.ZuoraCreatedDate),
            paymentMethod = billDetails.zuoraPaymentMethod,
            planName = billDetails.accountProductPlanName,
            planCost = billDetails.ZAmountWithoutTax,
            salesTaxCost = billDetails.ZtaxAmount,
            promoCode = billDetails.zuoraPaymentMethod,
            totalCost = billDetails.ZuoraAmountc,
            billingAddress = formatBillingAddress(billDetails)
        )
    }

    data class UiStatementDetails(
        val successfullyProcessed: String,
        val paymentMethod: String,
        val planName: String,
        val planCost: String,
        val salesTaxCost: String,
        val promoCode: String,
        val totalCost: String,
        val billingAddress: String
    )
}