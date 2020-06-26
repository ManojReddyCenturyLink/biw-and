package com.centurylink.biwf.screens.subscription

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SubscriptionCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.BillingAddress
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.account.RecordsItem
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val zuoraPaymentRepository: ZuoraPaymentRepository,
    private val accountRepository: AccountRepository,
    private val preferences: Preferences
) : BaseViewModel() {

    val myState = EventFlow<SubscriptionCoordinatorDestinations>()
    private lateinit var userAccount: AccountDetails

    private var serviceAddressData: BillingAddress = BillingAddress()
    private var billingAddress: BillingAddress = BillingAddress()

    val planName: Flow<String> = BehaviorStateFlow()
    val planDetails: Flow<String> = BehaviorStateFlow()
    val subscriptionUrl: Flow<String> = BehaviorStateFlow()
    val invoicesListResponse: Flow<PaymentList> = BehaviorStateFlow()
    var progressViewFlow = EventFlow<Boolean>()
    var errorMessageFlow = EventFlow<String>()

    init {
        progressViewFlow.latestValue = true
        initApis()
    }

    fun initApis() {
        viewModelScope.launch {
            requestAccountDetails()
            requestInvoiceList()
        }
        subscriptionUrl.latestValue = BASE_SUBSCRIPTION_URL + preferences.getValueByID(Preferences.USER_ID)
    }

    private suspend fun requestAccountDetails() {
        val userAccountDetails = accountRepository.getAccountDetails()
        userAccountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
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
        planName.latestValue = userAccount.productNameC ?: "Best in World Fiber "
        planDetails.latestValue = userAccount.productPlanNameC ?: "Speeds up to 940Mbps "
    }

    fun launchStatement(item: RecordsItem) {
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
        myState.latestValue = SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION
    }

    private suspend fun requestInvoiceList() {
        val paymentList = zuoraPaymentRepository.getInvoicesList()
        paymentList.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            invoicesListResponse.latestValue = it
            progressViewFlow.latestValue = false
        }
    }
    companion object {
        const val BASE_SUBSCRIPTION_URL = "https://qa-qa101.cs16.force.com/fiber/apex/vf_fiberBuyFlowPaymentMobile?userId="
    }
}
