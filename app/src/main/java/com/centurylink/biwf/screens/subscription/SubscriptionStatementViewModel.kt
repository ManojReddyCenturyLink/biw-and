package com.centurylink.biwf.screens.subscription

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.BillingRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionStatementViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val billingRepository: BillingRepository
) : BaseViewModel() {

    val paymentMethod: LiveData<String> = MutableLiveData()
    val successfullyProcessed: LiveData<String> = MutableLiveData()
    val emails: LiveData<String> = MutableLiveData()
    val billingAddress: LiveData<String> = MutableLiveData()
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

                accountDetails.billingAddress.apply {
                    billingAddress.latestValue =
                        "$street $state $postalCode $country"
                }
            } catch (e: Throwable) {

            }
        }
    }

    private fun getBillingInformation(){
        viewModelScope.launch {
            try {
                val billingDetaillist = billingRepository.getBillingDetails()
                var billDetails = billingDetaillist[0]
                planName.latestValue = billDetails.accountProductPlanName
                planCost.latestValue = billDetails.invoiceZuoraAccountWithoutTax
                salesTaxCost.latestValue = billDetails.ZtaxAmount
                paymentMethod.latestValue = billDetails.zuoraPaymentMethod
                promoCode.latestValue = billDetails.zuoraPaymentMethod
                promoCodeCost.latestValue
                promoCodeSubValue.latestValue
                totalCost.latestValue = billDetails.ZuoraAmountc
            } catch (e: Throwable) {
                Log.i("JAMMY","Exception Catch "+e.message)
            }
        }
    }
}