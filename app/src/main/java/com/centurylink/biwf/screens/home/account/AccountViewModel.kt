package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.AccountCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactRepository: ContactRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    val accountDetailsInfo: Flow<UiAccountDetails> = BehaviorStateFlow()
    var uiAccountDetails: UiAccountDetails = UiAccountDetails()
    var errorMessageFlow = EventFlow<String>()

    init {
        initApiCalls()
    }

    val myState = EventFlow<AccountCoordinatorDestinations>()

    val navigateToSubscriptionActivityEvent: EventLiveData<Unit> = MutableLiveData()

    fun onBiometricChange(boolean: Boolean) {
        uiAccountDetails = uiAccountDetails.copy(biometricStatus = boolean)
    }

    private fun initApiCalls() {
        viewModelScope.launch {
            requestUserInfo()
            getUserDetails()
            getAccountInfo()
            getContactInfo()
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
        navigateToSubscriptionActivityEvent.emit(Unit)
    }

    fun onPersonalInfoCardClick() {
        myState.latestValue = AccountCoordinatorDestinations.PROFILE_INFO
    }

    private suspend fun requestUserInfo() {
        val userInfo = userRepository.getUserInfo()
        userInfo.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {}
    }

    private suspend fun getUserDetails() {
        val userDetails = userRepository.getUserDetails()
        userDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateUIAccountDetailsFromUserDetails(it)
        }
    }

    private suspend fun getAccountInfo() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateUIAccountDetailsFromAccounts(it)

        }
    }

    private suspend fun getContactInfo() {
        val contactDetails = contactRepository.getContactDetails()
        contactDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateUIAccountDetailsFromContacts(it)
        }
    }

    private fun updateUIAccountDetailsFromAccounts(accontDetails: AccountDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            name = accontDetails.name,
            serviceAddress = accontDetails.serviceCompleteAddress,
            planName = accontDetails.productPlanNameC,
            planSpeed = accontDetails.productPlanNameC,
            paymentDate = DateUtils.formatInvoiceDate(accontDetails.lastViewedDate!!),
            password = "******", cellPhone = accontDetails.phone, homePhone = accontDetails.phone,
            workPhone = accontDetails.phone, serviceCallsAndText = accontDetails.cellPhoneOptInC
        )
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

    private fun updateUIAccountDetailsFromUserDetails(userDetails: UserDetails) {
        uiAccountDetails = uiAccountDetails.copy(email = userDetails.email)
    }

    data class UiAccountDetails(
        val name: String? = null,
        val serviceAddress: String? = null,
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
