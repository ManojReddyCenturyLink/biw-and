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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val zuoraPaymentRepository: ZuoraPaymentRepository,
    accountRepository: AccountRepository
) : BaseViewModel() {

    val myState = EventFlow<SubscriptionCoordinatorDestinations>()
    private lateinit var userAccount: AccountDetails

    private lateinit var serviceAddressData: BillingAddress
    private lateinit var billingAddress: BillingAddress
    private var uiSubscriptionPageObject = UiSubscriptionPageInfo()

    val checkboxState: Flow<Boolean> = BehaviorStateFlow(false)
    val uiFlowable: Flow<UiSubscriptionPageInfo> = BehaviorStateFlow()
    val planName: Flow<String> = BehaviorStateFlow()
    val planDetails: Flow<String> = BehaviorStateFlow()
    val invoicesListResponse: Flow<PaymentList> = BehaviorStateFlow()

    init {
        viewModelScope.launch {
            try {
                userAccount = accountRepository.getAccountDetails()

                billingAddress = userAccount.billingAddress

                serviceAddressData = BillingAddress(
                    street = userAccount.serviceStreet ?: billingAddress.street,
                    city = userAccount.serviceCity ?: billingAddress.city,
                    state = userAccount.serviceStateProvince ?: billingAddress.state,
                    postalCode = userAccount.servicePostalCode ?: billingAddress.postalCode
                )

                uiSubscriptionPageObject = uiSubscriptionPageObject.copy(
                    paymentFirstName = userAccount.firstName,
                    paymentlastName = userAccount.lastName,
                    billingFirstName = userAccount.firstName,
                    billingLastName = userAccount.lastName,
                    billingAddress = billingAddress
                )
                uiFlowable.latestValue = uiSubscriptionPageObject
                planName.latestValue = userAccount.productNameC!!
                planDetails.latestValue = userAccount.productPlanNameC!!

                getInvoicesList()
            } catch (e: Throwable) {
            }
        }

        mockPlanName()
    }

    private fun mockPlanName() {
        //this is so that the ui doesnt show an empty plan and plan description. Will remove in near future
        planName.latestValue = "Best in World Fiber"
        planDetails.latestValue = "Speeds up to 940Mbps"
    }

    fun sameAsServiceAddressedClicked() {
        checkboxState.latestValue = !checkboxState.latestValue
        if (checkboxState.latestValue) {
            uiSubscriptionPageObject = uiSubscriptionPageObject.copy(
                billingFirstName = userAccount.firstName,
                billingLastName = userAccount.lastName,
                billingAddress = serviceAddressData
            )
            uiFlowable.latestValue = uiSubscriptionPageObject
        }
    }

    fun launchStatement(item: RecordsItem) {
        val bundle = Bundle()
        bundle.putString(
            SubscriptionStatementActivity.SUBSCRIPTION_STATEMENT_TITLE,
            item.id
        )
        SubscriptionCoordinatorDestinations.bundle = bundle
        myState.latestValue = SubscriptionCoordinatorDestinations.STATEMENT
    }

    fun launchManageSubscription() {
        myState.latestValue = SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION
    }

    fun onPaymentFirstNameChange(firstName: String) {
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(paymentFirstName = firstName)
        uiFlowable.latestValue = uiSubscriptionPageObject
    }

    fun onPaymentLastNameChange(lastName: String) {
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(paymentlastName = lastName)
        uiFlowable.latestValue = uiSubscriptionPageObject
    }

    fun onBillingFirstNameChange(firstName: String) {
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(billingFirstName = firstName)
        uiFlowable.latestValue = uiSubscriptionPageObject
    }

    fun onBillingLastNameChange(lastName: String) {
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(billingLastName = lastName)
        uiFlowable.latestValue = uiSubscriptionPageObject
    }

    fun onStreetAddressChange(streetAddress: String) {
        billingAddress = billingAddress.copy(street = streetAddress)
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(billingAddress = billingAddress)
        uiFlowable.latestValue = uiSubscriptionPageObject
        checkboxState.latestValue = false
    }

    fun onCityChange(city: String) {
        billingAddress = billingAddress.copy(city = city)
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(billingAddress = billingAddress)
        uiFlowable.latestValue = uiSubscriptionPageObject
        checkboxState.latestValue = false
    }

    fun onStateChange(state: String) {
        billingAddress = billingAddress.copy(state = state)
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(billingAddress = billingAddress)
        uiFlowable.latestValue = uiSubscriptionPageObject
        checkboxState.latestValue = false
    }

    fun onZipCodeChange(zipCode: String) {
        billingAddress = billingAddress.copy(postalCode = zipCode)
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(billingAddress = billingAddress)
        uiFlowable.latestValue = uiSubscriptionPageObject
        checkboxState.latestValue = false
    }

    private suspend fun getInvoicesList() {
        try {
            val paymentList = zuoraPaymentRepository.getInvoicesList()
            invoicesListResponse.latestValue = paymentList
        } catch (e: Throwable) {
        }
    }

    data class UiSubscriptionPageInfo(
        val paymentFirstName: String? = null,
        val paymentlastName: String? = null,
        val creditCardNumber: String = "1234 - 1234 - 1234 - 1234",
        val expirationDate: String = "01 / 20",
        val cvv: String = "123",
        val billingFirstName: String? = null,
        val billingLastName: String? = null,
        val billingAddress: BillingAddress? = null
    )
}