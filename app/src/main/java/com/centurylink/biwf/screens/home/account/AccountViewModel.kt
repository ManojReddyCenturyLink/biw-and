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
        requestUserInfo()
    }

    val myState = EventFlow<AccountCoordinatorDestinations>()

    val navigateToSubscriptionActivityEvent: EventLiveData<Unit> = MutableLiveData()

    fun onBiometricChange(boolean: Boolean) {
        uiAccountDetails = uiAccountDetails.copy(biometricStatus = boolean)
    }

    fun onServiceCallsAndTextsChange(servicecall: Boolean) {
        viewModelScope.launch {
            try {
                uiAccountDetails = uiAccountDetails.copy(serviceCallsAndText = servicecall)
                accountRepository.setServiceCallsAndTexts(servicecall)
            } catch (e: Throwable) {

            }
        }
    }


    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                uiAccountDetails = uiAccountDetails.copy(marketingEmails = boolean)
                contactRepository.setMarketingEmails(boolean)
            } catch (e: Throwable) {

            }
        }
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                uiAccountDetails = uiAccountDetails.copy(marketingCallsAndText = boolean)
                contactRepository.setMarketingCallsAndText(boolean)
            } catch (e: Throwable) {

            }
        }
    }

    fun onResume() {
        updateAccountFlow()
    }

    fun onSubscriptionCardClick() {
        navigateToSubscriptionActivityEvent.emit(Unit)
    }

    fun onPersonalInfoCardClick() {
        myState.latestValue = AccountCoordinatorDestinations.PROFILE_INFO
    }

    private fun requestUserInfo() {
        viewModelScope.launch {
            val userInfo = userRepository.getUserInfo()
            userInfo.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {
                getUserDetails()
            }
        }
    }

    private fun getUserDetails() {
        viewModelScope.launch {
            val userDetails = userRepository.getUserDetails()
            userDetails.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {
                updateUIAccountDetailsFromUserDetails(it)
                getAccountInfo()
            }
        }
    }

    private fun getAccountInfo() {
        viewModelScope.launch {
            val accountDetails = accountRepository.getAccountDetails()
            accountDetails.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {
                updateUIAccountDetailsFromAccounts(it)
                getContactInfo()
            }
        }
    }

    private fun getContactInfo() {
        viewModelScope.launch {
            val contactDetails = contactRepository.getContactDetails()
            contactDetails.fold(ifLeft = {
                errorMessageFlow.latestValue = it
            }) {
                updateUIAccountDetailsFromContacts(it)
            }
        }
    }

    private fun updateUIAccountDetailsFromAccounts(accontDetails: AccountDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            name = accontDetails.name,
            serviceAddress = accontDetails.serviceCompleteAddress,
            planName = accontDetails.productPlanNameC,
            planSpeed = accontDetails.productPlanNameC,
            paymentDate = DateUtils.formatInvoiceDate(accontDetails.lastViewedDate),
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
