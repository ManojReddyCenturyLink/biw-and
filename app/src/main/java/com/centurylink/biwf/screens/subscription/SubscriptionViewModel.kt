package com.centurylink.biwf.screens.subscription

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SubscriptionCoordinator
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.account.RecordsItem
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.repos.ZuoraPaymentRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.ObservableData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val zuoraPaymentRepository: ZuoraPaymentRepository,
    accountRepository: AccountRepository
) : BaseViewModel() {

    val myState = ObservableData(SubscriptionCoordinator.SubscriptionCoordinatorDestinations.SUBSCRIPTION)

    private lateinit var userAccount: AccountDetails
    val checkboxState: Flow<Boolean> = BehaviorStateFlow(false)
    val paymentFirstName: Flow<String> = BehaviorStateFlow()
    val paymentLastName: Flow<String> = BehaviorStateFlow()
    val billingFirstName: Flow<String> = BehaviorStateFlow()
    val billingLastName: Flow<String> = BehaviorStateFlow()
    val billingStreetAddress: Flow<String> = BehaviorStateFlow()
    val billingCity: Flow<String> = BehaviorStateFlow()
    val billingState: Flow<String> = BehaviorStateFlow()
    val billingZipCode: Flow<String> = BehaviorStateFlow()
    val planName: Flow<String> = BehaviorStateFlow()
    val planDetails: Flow<String> = BehaviorStateFlow()
    val invoicesListResponse: Flow<PaymentList> = BehaviorStateFlow()

    init {
        viewModelScope.launch {
            try {
                userAccount = accountRepository.getAccountDetails()
                paymentFirstName.latestValue = userAccount.firstName
                paymentLastName.latestValue = userAccount.lastName
                billingFirstName.latestValue = userAccount.firstName
                billingLastName.latestValue = userAccount.lastName
                billingStreetAddress.latestValue = userAccount.billingAddress.street
                billingCity.latestValue = userAccount.billingAddress.city
                billingZipCode.latestValue = userAccount.billingAddress.postalCode
                billingState.latestValue = userAccount.billingAddress.state
                planName.latestValue = userAccount.productNameC
                planDetails.latestValue = userAccount.productPlanNameC
            } catch (e: Throwable) {
            }
        }
        getInvoicesList()
        mockPlanName()
    }

    private fun mockPlanName() {
        //this is so that the ui doesnt show an empty plan and plan description. Will remove in near future
        planName.latestValue = "Best in World Fiber"
        planDetails.latestValue = "Speeds up to 940Mbps"
    }

    fun navigateToInvoiceDetails() {
        myState.value = SubscriptionCoordinator.SubscriptionCoordinatorDestinations.STATEMENT
    }

    fun sameAsServiceAddressedClicked() {
        checkboxState.latestValue = !checkboxState.latestValue
        if (checkboxState.latestValue) {
            populateBillingAddress()
        }
    }

    fun launchStatement(item: RecordsItem) {
        var bundle = Bundle()
        bundle.putString(
            SubscriptionStatementActivity.SUBSCRIPTION_STATEMENT_TITLE,
            item.id
        )
        SubscriptionCoordinator.SubscriptionCoordinatorDestinations.bundle = bundle
        myState.value = SubscriptionCoordinator.SubscriptionCoordinatorDestinations.STATEMENT
    }

    fun launchManageSubscription() {
        myState.value =
            SubscriptionCoordinator.SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION
    }

    fun onPaymentFirstNameChange(firstName: String) {
        paymentFirstName.latestValue = firstName
    }

    fun onPaymentLastNameChange(lastName: String) {
        paymentLastName.latestValue = lastName
    }

    fun onBillingFirstNameChange(firstName: String) {
        billingFirstName.latestValue = firstName
        checkboxState.latestValue = false
    }

    fun onBillingLastNameChange(lastName: String) {
        billingLastName.latestValue = lastName
        checkboxState.latestValue = false
    }

    fun onStreetAddressChange(streetAddress: String) {
        billingStreetAddress.latestValue = streetAddress
        checkboxState.latestValue = false
    }

    fun onCityChange(city: String) {
        billingCity.latestValue = city
        checkboxState.latestValue = false
    }

    fun onStateChange(state: String) {
        billingState.latestValue = state
        checkboxState.latestValue = false
    }

    fun onZipCodeChange(zipCode: String) {
        billingZipCode.latestValue = zipCode
        checkboxState.latestValue = false
    }

    private fun populateBillingAddress() {
        billingFirstName.latestValue = userAccount.firstName
        billingLastName.latestValue = userAccount.lastName
        billingStreetAddress.latestValue = userAccount.billingAddress.street
        billingCity.latestValue = userAccount.billingAddress.city
        billingZipCode.latestValue = userAccount.billingAddress.postalCode
        billingState.latestValue = userAccount.billingAddress.state
    }

    private fun getInvoicesList() {
        viewModelScope.launch {
            try {
                val paymentList = zuoraPaymentRepository.getInvoicesList()
                invoicesListResponse.latestValue = paymentList
            } catch (e: Throwable) {
            }
        }
    }
}