package com.centurylink.biwf.screens.home.account

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

    lateinit var accountFlow: Flow<AccountDetails>
    lateinit var accountErrorFlow: Flow<Throwable>
    lateinit var onServiceCalls: Flow<Unit>
    lateinit var accountErrorSubmission: Flow<Throwable>
    lateinit var onMarketingEmailflow: Flow<Unit>
    lateinit var onMarketingcallsandTextflow: Flow<Unit>

    lateinit var contactFlow: Flow<ContactDetails>
    lateinit var contactErrorFlow: Flow<Throwable>
    lateinit var contactErrorSubmission: Flow<Throwable>

    lateinit var accountDetails: AccountDetails
    lateinit var contactDetails: ContactDetails

    init {
        requestAccountDetails()
        requestContactDetails()
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

    val marketingEmailStatus: EventLiveData<Boolean> = MutableLiveData()
    val serviceCallsAndTextStatus: EventLiveData<Boolean> = MutableLiveData()
    val marketingCallsAndTextStatus: EventLiveData<Boolean> = MutableLiveData()

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

    fun onServiceCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                onServiceCalls = accountRepository.setServiceCallsAndTexts(boolean)
            } catch (e: Throwable) {
                accountErrorSubmission.latestValue = e
            }
        }

    }

    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                onMarketingEmailflow = contactRepository.setMarketingEmails(boolean)
            } catch (e: Throwable) {
                contactErrorSubmission.latestValue = e
            }
        }
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                onMarketingcallsandTextflow = contactRepository.setMarketingCallsAndText(boolean)
            } catch (e: Throwable) {
                contactErrorSubmission.latestValue = e
            }
        }
    }

    fun onSubscriptionCardClick() {
        navigateToSubscriptionActivityEvent.emit(Unit)
    }

    private fun requestAccountDetails() {
        viewModelScope.launch {
            try {
                accountFlow = accountRepository.getAccountDetails()
                accountFlow.collect {
                    accountDetails = it
                }
                serviceCallsAndTextStatus.emit(accountDetails.marketingOptInC)
            } catch (e: Throwable) {
                accountErrorFlow.latestValue = e
            }
        }
    }

    private fun requestContactDetails() {
        viewModelScope.launch {
            try {
                contactFlow = contactRepository.getContactDetails()
                contactFlow.collect {
                    contactDetails = it
                }

            } catch (e: Throwable) {
                contactErrorFlow.latestValue = e
            }
        }
    }

}
