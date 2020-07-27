package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.AccountCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.*
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
    private val modemRebootMonitorService: ModemRebootMonitorService
) : BaseViewModel(modemRebootMonitorService) {

    class Factory @Inject constructor(
        private val accountRepository: AccountRepository,
        private val contactRepository: ContactRepository,
        private val userRepository: UserRepository,
        private val sharedPreferences: Preferences,
        private val authServiceFactory: AuthServiceFactory<*>,
        private val modemRebootMonitorService: ModemRebootMonitorService
    ) : ViewModelFactoryWithInput<AuthServiceHost> {

        override fun withInput(input: AuthServiceHost): ViewModelProvider.Factory {
            return viewModelFactory {
                AccountViewModel(
                    accountRepository,
                    contactRepository,
                    userRepository,
                    sharedPreferences,
                    authServiceFactory.create(input),
                    modemRebootMonitorService
                )
            }
        }
    }

    val accountDetailsInfo: Flow<UiAccountDetails> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val bioMetricFlow: Flow<Boolean> = BehaviorStateFlow(sharedPreferences.getBioMetrics() ?: false)
    var uiAccountDetails: UiAccountDetails = UiAccountDetails()
    var progressViewFlow = EventFlow<Boolean>()

    init {
        initApiCalls()
    }

    val myState = EventFlow<AccountCoordinatorDestinations>()

    val navigateToSubscriptionActivityEvent: EventLiveData<String> = MutableLiveData()

    fun onBiometricChange(boolean: Boolean) {
        sharedPreferences.saveBioMetrics(boolean)
    }

    fun initApiCalls() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestAccountDetails()
            requestContactInfo()
        }
    }

    fun onServiceCallsAndTextsChange(servicecall: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(serviceCallsAndText = servicecall)
            val result = accountRepository.setServiceCallsAndTexts(servicecall)
            errorMessageFlow.latestValue = result
        }
    }

    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(marketingEmails = boolean)
            val result = contactRepository.setMarketingEmails(boolean)
            errorMessageFlow.latestValue = result
        }
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            uiAccountDetails = uiAccountDetails.copy(marketingCallsAndText = boolean)
            val result = contactRepository.setMarketingCallsAndText(boolean)
            errorMessageFlow.latestValue = result
        }
    }

    fun onSubscriptionCardClick() {
        navigateToSubscriptionActivityEvent.emit(uiAccountDetails.paymentMethod ?: "Visa ******* 2453")
    }

    fun onPersonalInfoCardClick() {
        myState.latestValue = AccountCoordinatorDestinations.PROFILE_INFO
    }

    fun refreshBiometrics() {
        bioMetricFlow.latestValue = sharedPreferences.getBioMetrics() ?: false
    }

    fun onLogOutClick() {
        viewModelScope.launch {
            val result = authService.revokeToken()
            if (result) {
                sharedPreferences.saveBioMetrics(false)
                sharedPreferences.saveUserType(false)
                modemRebootMonitorService.cancelWork()
                myState.latestValue = AccountCoordinatorDestinations.LOG_IN
            } else {
                Timber.e("Auth Token Revoke Failed")
            }
        }
    }


    private suspend fun requestAccountDetails() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateUIAccountDetailsFromAccounts(it)
        }
    }

    private suspend fun requestContactInfo() {
        val contactDetails = contactRepository.getContactDetails()
        contactDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateUIAccountDetailsFromContacts(it)
            progressViewFlow.latestValue = false
        }
    }

    private fun updateUIAccountDetailsFromAccounts(accontDetails: AccountDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            name = accontDetails.name,
            serviceAddress1 = accontDetails.billingAddress?.street ?: "",
            serviceAddress2 = formatServiceAddress2(accontDetails) ?: "",
            email = accontDetails.emailAddress?:"",
            planName = accontDetails.productNameC ?: "",
            planSpeed = accontDetails.productPlanNameC ?: "",
            paymentDate = DateUtils.formatInvoiceDate(accontDetails.lastViewedDate!!),
            password = "******",
            cellPhone = PhoneNumber(accontDetails.phone?:"").toString(),
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
