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
    private val accountRepository: AccountRepository
) : BaseViewModel() {

    val myState = EventFlow<SubscriptionCoordinatorDestinations>()
    private lateinit var userAccount: AccountDetails

    private var serviceAddressData: BillingAddress = BillingAddress()
    private var billingAddress: BillingAddress = BillingAddress()
    private var uiSubscriptionPageObject = UiSubscriptionPageInfo()

    val checkboxState: Flow<Boolean> = BehaviorStateFlow(false)
    val uiFlowable: Flow<UiSubscriptionPageInfo> = BehaviorStateFlow()
    val planName: Flow<String> = BehaviorStateFlow()
    val planDetails: Flow<String> = BehaviorStateFlow()
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
        uiSubscriptionPageObject = uiSubscriptionPageObject.copy(
            paymentFirstName = userAccount.firstName,
            paymentlastName = userAccount.lastName,
            billingFirstName = userAccount.firstName,
            billingLastName = userAccount.lastName,
            billingAddress = billingAddress
        )
        uiFlowable.latestValue = uiSubscriptionPageObject
        planName.latestValue = userAccount.productNameC ?: "Best in World Fiber "
        planDetails.latestValue = userAccount.productPlanNameC ?: "Speeds up to 940Mbps "
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

    private suspend fun requestInvoiceList() {
        val paymentList = zuoraPaymentRepository.getInvoicesList()
        paymentList.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            invoicesListResponse.latestValue = it
            progressViewFlow.latestValue = false
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