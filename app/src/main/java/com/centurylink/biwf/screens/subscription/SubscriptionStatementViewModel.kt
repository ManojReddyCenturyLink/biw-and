package com.centurylink.biwf.screens.subscription

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.billing.BillingDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.BillingRepository
import com.centurylink.biwf.utility.DateUtils
import kotlinx.coroutines.launch
import java.util.stream.Collectors
import javax.inject.Inject

class SubscriptionStatementViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val billingRepository: BillingRepository
) : BaseViewModel() {

    val paymentMethod: LiveData<String> = MutableLiveData()
    val successfullyProcessed: LiveData<String> = MutableLiveData()
    val emails: LiveData<String> = MutableLiveData()
    val billingAddressData: LiveData<String> = MutableLiveData()
    val planName: LiveData<String> = MutableLiveData()
    val planCost: LiveData<String> = MutableLiveData()
    val salesTaxCost: LiveData<String> = MutableLiveData()
    val promoCode: LiveData<String> = MutableLiveData()
    val promoCodeCost: LiveData<String> = MutableLiveData()
    val promoCodeSubValue: LiveData<String> = MutableLiveData()
    val totalCost: LiveData<String> = MutableLiveData()

    init {
        getAccountInformation()
        getBillingInformation()
    }

    private fun getAccountInformation() {
        viewModelScope.launch {
            try {
                val accountDetails = accountRepository.getAccountDetails()
                emails.latestValue = accountDetails.emailAddress
            } catch (e: Throwable) {

            }
        }
    }

    private fun getBillingInformation() {
        viewModelScope.launch {
            try {
                val billingDetailList = billingRepository.getBillingDetails()
                var billDetails = billingDetailList[0]
                successfullyProcessed.latestValue =
                    DateUtils.formatPaymentProcessedDate(billDetails.ZuoraCreatedDate)
                paymentMethod.latestValue = billDetails.zuoraPaymentMethod
                formatBillingAddress(billDetails)
                planName.latestValue = billDetails.accountProductPlanName
                planCost.latestValue = billDetails.ZAmountWithoutTax

                salesTaxCost.latestValue = billDetails.ZtaxAmount
                promoCode.latestValue = billDetails.zuoraPaymentMethod

                totalCost.latestValue = billDetails.ZuoraAmountc
            } catch (e: Throwable) {
                Log.i("JAMMY", "Exception Catch " + e.message)
            }
        }
    }

    private fun formatBillingAddress(billDetails: BillingDetails) {
        billDetails.billingAddress.apply {
            val billingAddressList: MutableList<String> = mutableListOf<String>()
            billingAddressList.add(street)
            billingAddressList.add(city)
            billingAddressList.add(state)
            billingAddressList.add(postalCode)
            billingAddressList.add(country)
            val finalAddress: String = billingAddressList.filterNotNull().stream()
                .collect(Collectors.joining(" , "))
            billingAddressData.latestValue = finalAddress
        }
    }


}