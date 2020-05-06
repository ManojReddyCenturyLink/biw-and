package com.centurylink.biwf.screens.home.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.CommunicationRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.SubscriptionRepository
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    communicationRepository: CommunicationRepository,
    subscriptionRepository: SubscriptionRepository,
    private val accountRepository: AccountRepository,
    private val contactRepository: ContactRepository
) : BaseViewModel() {

    var accountFlow: Flow<AccountDetails> = MutableStateFlow()
    var contactFlow: Flow<ContactDetails> = MutableStateFlow()

    var contactErrorFlow: Flow<Throwable> = MutableStateFlow()
    var contactErrorSubmission: Flow<Throwable> = MutableStateFlow()
    var accountErrorFlow: Flow<Throwable> = MutableStateFlow()
    var accountErrorSubmission: Flow<Throwable> = MutableStateFlow()

    lateinit var accountDetails: AccountDetails
    lateinit var contactDetails: ContactDetails

    init {
        updateServiceandCallStatusForUser()
        requestMarketingEmailsAndTexts()
    }

    val accountName: LiveData<String> =
        MutableLiveData(accountRepository.getAccount().value?.fullName)
    val streetAddress: LiveData<String> =
        MutableLiveData(accountRepository.getAccount().value?.streetAddress)
    val city: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.city)
    val state: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.state)
    val zipcode: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.zipcode)
    val combinedSecondHalfOfAddress =
        MutableLiveData(city.value + ", " + state.value + " " + zipcode.value)
    val subscriptionName: LiveData<String> =
        MutableLiveData(subscriptionRepository.getSubscription().value?.subscriptionName)
    val subscriptionDescription: LiveData<String> =
        MutableLiveData(subscriptionRepository.getSubscription().value?.subscriptionDetails)
    val subscriptionDate: LiveData<String> =
        MutableLiveData(subscriptionRepository.getSubscription().value?.subscriptionDate)
    val subscriptionCreditCardType: LiveData<String> =
        MutableLiveData(subscriptionRepository.getSubscription().value?.subscriptionCardType)
    val subscriptionCard4Digits: LiveData<String> =
        MutableLiveData(subscriptionRepository.getSubscription().value?.subscriptionCardDigits)
    val subscriptionCardDisplayedText: LiveData<String> =
        MutableLiveData(subscriptionCreditCardType.value + " ********" + subscriptionCard4Digits.value)
    val biometricType: LiveData<String> = MutableLiveData()
    val biometricStatus: LiveData<Boolean> =
        MutableLiveData(communicationRepository.getPreferences().value?.biometricStatus)

    val serviceCallsAndTextStatus: LiveData<Boolean> = MutableLiveData(false)
    val marketingEmailStatus: LiveData<Boolean> = MutableLiveData(false)
    val marketingCallsAndTextStatus: LiveData<Boolean> = MutableLiveData(false)

    val cellNumber: LiveData<String> =
        MutableLiveData(accountRepository.getAccount().value?.cellNumber)
    val homeNumber: LiveData<String> =
        MutableLiveData(accountRepository.getAccount().value?.homeNumber)
    val workNumber: LiveData<String> =
        MutableLiveData(accountRepository.getAccount().value?.workNumber)
    val emailAddress: LiveData<String> =
        MutableLiveData(accountRepository.getAccount().value?.emailAddress)

    val navigateToSubscriptionActivityEvent: EventLiveData<Unit> = MutableLiveData()

    fun onBiometricChange(boolean: Boolean) {
        biometricStatus.latestValue = boolean
    }

    fun onServiceCallsAndTextsChange(email: Boolean) {
        viewModelScope.launch {
            try {
                accountRepository.setServiceCallsAndTexts(email)
            } catch (e: Throwable) {
                accountErrorSubmission.latestValue = e
            }
        }
    }

    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                contactRepository.setMarketingEmails(boolean)
            } catch (e: Throwable) {
                contactErrorFlow.latestValue = e
            }
        }
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                contactRepository.setMarketingCallsAndText(boolean)
            } catch (e: Throwable) {
                contactErrorSubmission.latestValue = e
            }
        }
    }

    fun onSubscriptionCardClick() {
        navigateToSubscriptionActivityEvent.emit(Unit)
    }

    private fun updateServiceandCallStatusForUser() {
        viewModelScope.launch {
            try {
                accountFlow.latestValue = accountRepository.getAccountDetails()
                accountFlow.collect {
                    accountDetails = it
                    serviceCallsAndTextStatus.latestValue = accountDetails.emailOptInC
                }
            } catch (e: Throwable) {
                accountErrorFlow.latestValue = e
            }
        }
    }

    private fun requestMarketingEmailsAndTexts() {
        viewModelScope.launch {
            try {
                contactFlow.latestValue = contactRepository.getContactDetails()
                contactFlow.collect {
                    contactDetails = it
                    marketingEmailStatus.latestValue = contactDetails.emailOptInC
                    marketingCallsAndTextStatus.latestValue = contactDetails.marketingOptInC
                }
            } catch (e: Throwable) {
                contactErrorFlow.latestValue = e
            }
        }
    }
}
