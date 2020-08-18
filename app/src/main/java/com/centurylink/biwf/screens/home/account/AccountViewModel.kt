package com.centurylink.biwf.screens.home.account

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.AccountCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentInfo
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.PhoneNumber
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.preferences.Preferences
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AccountViewModel internal constructor(
    private val accountRepository: AccountRepository,
    private val contactRepository: ContactRepository,
    private val userRepository: UserRepository,
    private val sharedPreferences: Preferences,
    private val authService: AuthService<*>,
    private val modemRebootMonitorService: ModemRebootMonitorService,
    private val analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    class Factory @Inject constructor(
        private val accountRepository: AccountRepository,
        private val contactRepository: ContactRepository,
        private val userRepository: UserRepository,
        private val sharedPreferences: Preferences,
        private val authServiceFactory: AuthServiceFactory<*>,
        private val modemRebootMonitorService: ModemRebootMonitorService,
        private val analyticsManagerInterface: AnalyticsManager
    ) : ViewModelFactoryWithInput<AuthServiceHost> {

        override fun withInput(input: AuthServiceHost): ViewModelProvider.Factory {
            return viewModelFactory {
                AccountViewModel(
                    accountRepository,
                    contactRepository,
                    userRepository,
                    sharedPreferences,
                    authServiceFactory.create(input),
                    modemRebootMonitorService,
                    analyticsManagerInterface
                )
            }
        }
    }

    val accountDetailsInfo: Flow<UiAccountDetails> = BehaviorStateFlow()
    val paymentInfo: Flow<PaymentInfo> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val bioMetricFlow: Flow<Boolean> = BehaviorStateFlow(sharedPreferences.getBioMetrics() ?: false)
    var uiAccountDetails: UiAccountDetails = UiAccountDetails()
    var progressViewFlow = EventFlow<Boolean>()

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_ACCOUNTS)
        initApiCalls()
    }

    val myState = EventFlow<AccountCoordinatorDestinations>()

    val navigateToSubscriptionActivityEvent: EventLiveData<String> = MutableLiveData()

    fun initApiCalls() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestAccountDetails()
            requestContactInfo()
            requestCardInfo()
        }
    }

    fun onBiometricChange(boolean: Boolean) {
        sharedPreferences.saveBioMetrics(boolean)
        analyticsManagerInterface.logToggleChangeEvent(AnalyticsKeys.TOGGLE_BIOMETRIC, boolean)
    }

    fun onServiceCallsAndTextsChange(serviceCall: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(serviceCallsAndText = serviceCall)
            val result = accountRepository.setServiceCallsAndTexts(serviceCall)
            errorMessageFlow.latestValue = result
        }
        analyticsManagerInterface.logToggleChangeEvent(AnalyticsKeys.TOGGLE_SERVICE_CALLS_AND_TEXT, serviceCall)
    }

    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(marketingEmails = boolean)
            val result = contactRepository.setMarketingEmails(boolean)
            errorMessageFlow.latestValue = result
        }
        analyticsManagerInterface.logToggleChangeEvent(AnalyticsKeys.TOGGLE_MARKETING_EMAILS, boolean)
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(marketingCallsAndText = boolean)
            val result = contactRepository.setMarketingCallsAndText(boolean)
            errorMessageFlow.latestValue = result
        }
        analyticsManagerInterface.logToggleChangeEvent(AnalyticsKeys.TOGGLE_MARKETING_CALLS_AND_TEXT, boolean)
    }

    fun onSubscriptionCardClick() {
        analyticsManagerInterface.logCardClickEvent(AnalyticsKeys.CARD_SUBSCRIPTION_INFO)
        navigateToSubscriptionActivityEvent.emit(uiAccountDetails.paymentMethod ?: "")
    }

    fun onPersonalInfoCardClick() {
        analyticsManagerInterface.logCardClickEvent(AnalyticsKeys.CARD_PERSONAL_INFO)
        val bundle = Bundle()
        bundle.putString(PersonalInfoActivity.USER_ID, accountDetailsInfo.latestValue.email)
        AccountCoordinatorDestinations.bundle = bundle
        myState.latestValue = AccountCoordinatorDestinations.PROFILE_INFO
    }

    fun refreshBiometrics() {
        bioMetricFlow.latestValue = sharedPreferences.getBioMetrics() ?: false
    }

    fun onLogOutClick() {
        viewModelScope.launch {
            val result = authService.revokeToken()
            if (result) {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.LOG_OUT_SUCCESS)
                sharedPreferences.clearUserSettings()
                modemRebootMonitorService.cancelWork()
                myState.latestValue = AccountCoordinatorDestinations.LOG_IN
            } else {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.LOG_OUT_FAILURE)
                Timber.e("Auth Token Revoke Failed")
            }
        }
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_LOG_OUT)
    }

    private suspend fun requestAccountDetails() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_SUCCESS)
            updateUIAccountDetailsFromAccounts(it)
        }
    }

    private suspend fun requestContactInfo() {
        val contactDetails = contactRepository.getContactDetails()
        contactDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_CONTACT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_CONTACT_DETAILS_SUCCESS)
            updateUIAccountDetailsFromContacts(it)
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun requestCardInfo() {
        val cardInfoResponse = accountRepository.getLiveCardDetails()
        cardInfoResponse.fold(
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_LIVE_CARD_INFO_FAILURE)
                errorMessageFlow.latestValue = it
            }
        ) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_LIVE_CARD_INFO_SUCCESS)
            if (it.isDone) {
                paymentInfo.latestValue = it.list[0]
                updateUIAccountDetailsFromLivePaymentInfo(paymentInfo.latestValue.creditCardSummary)
            }
        }
    }

    private fun updateUIAccountDetailsFromAccounts(accontDetails: AccountDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            name = accontDetails.name,
            serviceAddress1 = accontDetails.serviceCompleteAddress ?: "",
            serviceAddress2 = formatServiceAddress2(accontDetails) ?: "",
            email = accontDetails.emailAddress ?: "",
            planName = accontDetails.productNameC ?: "",
            planSpeed = accontDetails.productPlanNameC ?: "",
            paymentDate = DateUtils.formatInvoiceDate(accontDetails.lastViewedDate!!),
            password = "******",
            cellPhone = PhoneNumber(accontDetails.phone ?: "").toString(),
            homePhone = accontDetails.phone,
            workPhone = accontDetails.phone,
            serviceCallsAndText = accontDetails.cellPhoneOptInC,
            paymentMethod = accontDetails.paymentMethodName
        )
    }

    private fun formatServiceAddress2(accountDetails: AccountDetails): String? {
        return accountDetails.billingAddress?.run {
            val billingAddressList = listOf(city, state, postalCode, country)
            billingAddressList.filterNotNull().joinToString(separator = ", ")
        }
    }

    private fun updateUIAccountDetailsFromContacts(contactDetails: ContactDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            marketingEmails = contactDetails.emailOptInC,
            marketingCallsAndText = contactDetails.marketingOptInC
        )
        updateAccountFlow()
    }

    private fun updateUIAccountDetailsFromLivePaymentInfo(cardNumbers: String) {
        uiAccountDetails = uiAccountDetails.copy(
            paymentMethod = cardNumbers
        )
        updateAccountFlow()
    }

    private fun updateAccountFlow() {
        accountDetailsInfo.latestValue = uiAccountDetails
    }

    data class UiAccountDetails(
        val name: String? = null,
        val serviceAddress1: String? = null,
        val serviceAddress2: String? = null,
        val planName: String? = null,
        val planSpeed: String? = null,
        val paymentDate: String? = null,
        val paymentMethod: String? = null,
        val email: String? = null,
        val password: String? = null,
        val cellPhone: String? = null,
        val homePhone: String? = null,
        val workPhone: String? = null,
        val biometricStatus: Boolean = false,
        val serviceCallsAndText: Boolean = false,
        val marketingEmails: Boolean = true,
        val marketingCallsAndText: Boolean = false
    )
}
