package com.centurylink.biwf.screens.home.account

import android.content.Context
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
import com.centurylink.biwf.repos.ZouraSubscriptionRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.*
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Account view model
 *
 * @property accountRepository - repository instance to handle account api calls
 * @property contactRepository - repository instance to handle contact api calls
 * @property userRepository - repository instance to handle user api calls
 * @property sharedPreferences - preferences instance to handle shared preferences
 * @property authService - service instance to handle auth api calls
 * @property modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @constructor
 *
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class AccountViewModel internal constructor(
    private val accountRepository: AccountRepository,
    private val contactRepository: ContactRepository,
    private val userRepository: UserRepository,
    private val zouraSubscriptionRepository: ZouraSubscriptionRepository,
    private val sharedPreferences: Preferences,
    private val authService: AuthService<*>,
    private val modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    class Factory @Inject constructor(
        private val accountRepository: AccountRepository,
        private val contactRepository: ContactRepository,
        private val userRepository: UserRepository,
        private val zouraSubscriptionRepository: ZouraSubscriptionRepository,
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
                    zouraSubscriptionRepository,
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
    var noInternetMessage = EventFlow<Boolean>()
    val bioMetricFlow: Flow<Boolean> = BehaviorStateFlow(sharedPreferences.getBioMetrics() ?: false)
    var uiAccountDetails: UiAccountDetails = UiAccountDetails()
    var progressViewFlow = EventFlow<Boolean>()
    var userPhoneNumberUpdateFlow = EventFlow<String>()

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        initApiCalls()
    }

    val myState = EventFlow<AccountCoordinatorDestinations>()

    val navigateToSubscriptionActivityEvent: EventLiveData<String> = MutableLiveData()

    /**
     * Init api calls - It will start all the api calls initialisation
     *
     */
    fun initApiCalls() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestAccountDetails()
            requestContactInfo()
            requestCardInfo()
            requestSubscriptionDetails()
        }
    }

    /**
     * Init account and contact api calls - It will initializes account and contact api calls
     *
     */
    fun initAccountAndContactApiCalls() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestAccountDetails()
            requestContactInfo()
            progressViewFlow.latestValue = false
        }
    }

    /**
     * On biometric change - This is used to handle turning on/off toggle for biometric switch
     * change logic
     *
     * @param boolean - The boolean value to set biometric change
     */
    fun onBiometricChange(boolean: Boolean) {
        sharedPreferences.saveBioMetrics(boolean)
        analyticsManagerInterface.logToggleChangeEvent(AnalyticsKeys.TOGGLE_BIOMETRIC, boolean)
    }

    /**
     * On service calls and texts change - The user can enable/disable Service Calls AndTexts
     * messages by turning on/off toggle in Accounts Fragment
     *
     * @param serviceCall - The boolean valued used to set service
     */
    fun onServiceCallsAndTextsChange(serviceCall: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(serviceCallsAndText = serviceCall)
            val result = accountRepository.setServiceCallsAndTexts(serviceCall)
            errorMessageFlow.latestValue = result
        }
        analyticsManagerInterface.logToggleChangeEvent(
            AnalyticsKeys.TOGGLE_SERVICE_CALLS_AND_TEXT,
            serviceCall
        )
    }

    /**
     * On marketing emails change - This is used to handle turning on/off toggle for on marketing
     * email switch change logic
     *
     * @param boolean - The boolean value used to set on marketing emails changed click
     */
    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(marketingEmails = boolean)
            val result = contactRepository.setMarketingEmails(boolean)
            errorMessageFlow.latestValue = result
        }
        analyticsManagerInterface.logToggleChangeEvent(
            AnalyticsKeys.TOGGLE_MARKETING_EMAILS,
            boolean
        )
    }

    /**
     * On marketing calls and texts change - The user can enable/disable Service Calls And Texts
     * messages by turning on/off toggle in Accounts Fragment
     *
     * @param boolean - The boolean valued used to set service
     * @param phoneNumber - The phone number to used for marketing calls
     */
    fun onMarketingCallsAndTextsChange(boolean: Boolean, phoneNumber: String) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(marketingCallsAndText = boolean)
            val result = contactRepository.setMarketingCallsAndText(boolean, phoneNumber)
            userPhoneNumberUpdateFlow.latestValue = result
        }
        analyticsManagerInterface.logToggleChangeEvent(
            AnalyticsKeys.TOGGLE_MARKETING_CALLS_AND_TEXT,
            boolean
        )
    }

    /**
     * On subscription card click - It will navigate to subscription activity on click of
     * subscription card
     *
     */
    fun onSubscriptionCardClick() {
        analyticsManagerInterface.logCardClickEvent(AnalyticsKeys.CARD_SUBSCRIPTION_INFO)
        navigateToSubscriptionActivityEvent.emit(uiAccountDetails.paymentMethod ?: "")
    }

    /**
     * On personal info card click - It will navigate to personal info activity on click of
     * personal info card
     *
     */
    fun onPersonalInfoCardClick() {
        analyticsManagerInterface.logCardClickEvent(AnalyticsKeys.CARD_PERSONAL_INFO)
        val bundle = Bundle()
        bundle.putString(PersonalInfoActivity.USER_ID, accountDetailsInfo.latestValue.email)
        bundle.putString(
            PersonalInfoActivity.PHONE_NUMBER,
            NumberUtil.getOnlyDigits(accountDetailsInfo.latestValue.cellPhone)
        )
        AccountCoordinatorDestinations.bundle = bundle
        myState.latestValue = AccountCoordinatorDestinations.PROFILE_INFO
    }

    /**
     * Refresh biometrics
     *
     */
    fun refreshBiometrics() {
        bioMetricFlow.latestValue = sharedPreferences.getBioMetrics() ?: false
    }

    /**
     * Log screen launch - This is used to launch the screen on resume
     *
     */
    fun logScreenLaunch() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_ACCOUNTS)
    }

    /**
     * On log out click - This will handle log out button click event logic
     *
     * @param context - The activity context
     */
    fun onLogOutClick(context: Context) {
        if (AppUtil.isOnline(context)) {
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
        } else {
            noInternetMessage.latestValue = true
        }
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_LOG_OUT)
    }

    /**
     * Request account details - It is used to request account details through API call
     *
     */
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

    /**
     * Request contact info - It is used to request contact details through API call
     *
     */
    private suspend fun requestContactInfo() {
        val contactDetails = contactRepository.getContactDetails()
        contactDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_CONTACT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_CONTACT_DETAILS_SUCCESS)
            updateUIAccountDetailsFromContacts(it)
        }
    }

    /**
     * Request card info - It is used to request card details through API call
     *
     */
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
                val creditCardInfo = it.list.firstOrNull()?.creditCardSummary
                if (!creditCardInfo.isNullOrEmpty()) {
                    updateUIAccountDetailsFromLivePaymentInfo(creditCardInfo)
                }
            }
        }
    }

    /**
     * Update u i account details from accounts
     *
     * @param accountDetails - instance of account details repository
     */
    private fun updateUIAccountDetailsFromAccounts(accountDetails: AccountDetails) {
        var nextRenewalDate = "n/a"
        if (!accountDetails.nextRenewalDate.isNullOrEmpty()) {
            nextRenewalDate = DateUtils.formatAppointmentBookedDate(accountDetails.nextRenewalDate)
        }
        uiAccountDetails = uiAccountDetails.copy(
            name = accountDetails.name,
            formattedServiceAddressLine1 = formatServiceAddressLine1(
                accountDetails.serviceStreet ?: "",
                accountDetails.serviceUnit ?: ""
            ),
            formattedServiceAddressLine2 = formatServiceAddressLine2(
                accountDetails.serviceCity ?: "",
                accountDetails.serviceStateProvince ?: "",
                accountDetails.servicePostalCode ?: ""
            ),
            email = accountDetails.emailAddress ?: "",
            password = "******",
            cellPhone = PhoneNumber(accountDetails.phone ?: "").toString(),
            homePhone = accountDetails.phone,
            workPhone = accountDetails.phone,
            serviceCallsAndText = accountDetails.cellPhoneOptInC,
            paymentDate = nextRenewalDate
        )
    }

    /**
     * Format service address line1 - Used for formatting customer of service address line 1
     *
     * @param street - The street
     * @param unit - The unit
     */
    private fun formatServiceAddressLine1(street: String, unit: String) =
        if (unit.isNotBlank()) "$street $unit" else street

    /**
     * Format service address line2 - Used for formatting customer of service address line 2
     *
     * @param city
     * @param stateProvince
     * @param postalCode
     * @return
     */
    private fun formatServiceAddressLine2(
        city: String,
        stateProvince: String,
        postalCode: String
    ): String {
        val cityText = if (city.isNotBlank()) "$city, " else ""
        val stateProvinceText = if (stateProvince.isNotBlank()) "$stateProvince " else ""
        return cityText + stateProvinceText + postalCode
    }

    /**
     * Update u i account details from contacts
     *
     * @param contactDetails - instance contact details repository
     */
    private fun updateUIAccountDetailsFromContacts(contactDetails: ContactDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            marketingEmails = contactDetails.emailOptInC,
            marketingCallsAndText = contactDetails.marketingOptInC
        )
        updateAccountFlow()
    }

    /**
     * Update u i account details from live payment info
     *
     * @param creditCardInfo - The payment method to be updated
     */
    private fun updateUIAccountDetailsFromLivePaymentInfo(creditCardInfo: String) {
        uiAccountDetails = uiAccountDetails.copy(
            paymentMethod = creditCardInfo
        )
        updateAccountFlow()
    }

    /**
     * Update account flow - This will update account view flow with latest details
     *
     */
    private fun updateAccountFlow() {
        accountDetailsInfo.latestValue = uiAccountDetails
    }

    /**
     * Request Subscription Details - It is used to request Subscription details through API call
     *
     */

    private suspend fun requestSubscriptionDetails() {
        val userAccountDetails = zouraSubscriptionRepository.getSubscriptionDetails()
        userAccountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            progressViewFlow.latestValue = false
            uiAccountDetails = uiAccountDetails.copy(
                planName = it.records[0].zuora__ProductName__c ?: "",
                planSpeed = it.records[0].internetSpeed__c ?: ""
            )
            updatePlanDetails()
        }
    }
    /**
     * Update Subscription flow - This will update subscription view flow with latest details
     *
     */
    private fun updatePlanDetails() {
        accountDetailsInfo.latestValue = uiAccountDetails
    }

    data class UiAccountDetails(
        val name: String? = null,
        val formattedServiceAddressLine1: String = "",
        val formattedServiceAddressLine2: String = "",
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
