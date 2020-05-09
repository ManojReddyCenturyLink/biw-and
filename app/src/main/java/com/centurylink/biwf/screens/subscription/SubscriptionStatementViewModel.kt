package com.centurylink.biwf.screens.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.BillingRepository
import com.centurylink.biwf.repos.CancelSubscriptionRepository
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
}