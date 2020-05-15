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

    init {
        getUserDetails()
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
                updateAccountFlow()
                accountRepository.setServiceCallsAndTexts(servicecall)
            } catch (e: Throwable) {

            }
        }
    }

    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                uiAccountDetails = uiAccountDetails.copy(marketingEmails = boolean)
                updateAccountFlow()
                contactRepository.setMarketingEmails(boolean)
            } catch (e: Throwable) {

            }
        }
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                uiAccountDetails = uiAccountDetails.copy(marketingCallsAndText = boolean)
                updateAccountFlow()
                contactRepository.setMarketingCallsAndText(boolean)
            } catch (e: Throwable) {

            }
        }
    }

    fun onSubscriptionCardClick() {
        navigateToSubscriptionActivityEvent.emit(Unit)
    }

    fun onPersonalInfoCardClick() {
        myState.latestValue = AccountCoordinatorDestinations.PROFILE_INFO
    }

    private fun getAccountInfo() {
        viewModelScope.launch {
            try {
                val accountDetails = accountRepository.getAccountDetails()
                updateUIAccountDetailsFromAccounts(accountDetails)
                getContactInfo()
            } catch (e: Throwable) {
            }
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

    private fun getContactInfo() {
        viewModelScope.launch {
            try {
                val contactDetails = contactRepository.getContactDetails()
                updateUIAccountDetailsFromContacts(contactDetails)
            } catch (e: Throwable) {
            }
        }
    }

    private fun getUserDetails() {
        viewModelScope.launch {
            try {
                val userDetails = userRepository.getUserDetails()
                updateUIAccountDetailsFromUserDetails(userDetails)
                getAccountInfo()
            } catch (e: Throwable) {

            }
        }
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
