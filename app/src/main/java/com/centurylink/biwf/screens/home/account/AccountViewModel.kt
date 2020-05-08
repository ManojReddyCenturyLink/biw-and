package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.AccountCoordinator
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.CommunicationRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.SubscriptionRepository
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.ObservableData
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    communicationRepository: CommunicationRepository,
    subscriptionRepository: SubscriptionRepository,
    private val accountRepository: AccountRepository,
    private val contactRepository: ContactRepository
) : BaseViewModel() {

    init {
        updateServiceandCallStatusForUser()
        requestMarketingEmailsAndTexts()
    }

    val myState = ObservableData(AccountCoordinator.AccountCoordinatorDestinations.HOME)
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

            }
        }
    }

    fun onMarketingEmailsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                contactRepository.setMarketingEmails(boolean)
            } catch (e: Throwable) {

            }
        }
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        viewModelScope.launch {
            try {
                contactRepository.setMarketingCallsAndText(boolean)
            } catch (e: Throwable) {

            }
        }
    }

    fun onSubscriptionCardClick() {
        navigateToSubscriptionActivityEvent.emit(Unit)
    }

    fun onPersonalInfoCardClick() {
        myState.value = AccountCoordinator.AccountCoordinatorDestinations.PROFILE_INFO
    }

    private fun updateServiceandCallStatusForUser() {
        viewModelScope.launch {
            try {
                val accountFlow = accountRepository.getAccountDetails()
                serviceCallsAndTextStatus.latestValue = accountFlow.emailOptInC
            } catch (e: Throwable) {

            }
        }
    }

    private fun requestMarketingEmailsAndTexts() {
        viewModelScope.launch {
            try {
                val contactFlow = contactRepository.getContactDetails()
                marketingEmailStatus.latestValue = contactFlow.emailOptInC
                marketingCallsAndTextStatus.latestValue = contactFlow.marketingOptInC
            } catch (e: Throwable) {

            }
        }
    }
}
