package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.CommunicationRepository
import com.centurylink.biwf.repos.SubscriptionRepository
import com.centurylink.biwf.utility.EventLiveData
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    communicationRepository: CommunicationRepository,
    subscriptionRepository: SubscriptionRepository,
    accountRepository: AccountRepository
) : BaseViewModel() {


    val accountName: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.fullName)
    val streetAddress: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.streetAddress)
    val city: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.city)
    val state: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.state)
    val zipcode: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.zipcode)
    val combinedSecondHalfOfAddress = MutableLiveData(city.value + ", " + state.value + " " + zipcode.value)
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
        MutableLiveData(subscriptionCreditCardType.value + " ********"+ subscriptionCard4Digits.value)
    val biometricType: LiveData<String> = MutableLiveData()
    val biometricStatus: LiveData<Boolean> =
        MutableLiveData(communicationRepository.getPreferences().value?.biometricStatus)
    val marketingEmailStatus: LiveData<Boolean> =
        MutableLiveData(communicationRepository.getPreferences().value?.marketingEmailsStatus)
    val serviceCallsAndTextStatus: LiveData<Boolean> =
        MutableLiveData(communicationRepository.getPreferences().value?.serviceCallsStatus)
    val marketingCallsAndTextStatus: LiveData<Boolean> =
        MutableLiveData(communicationRepository.getPreferences().value?.marketingCallsAndTextStatus)
    val cellNumber: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.cellNumber)
    val homeNumber: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.homeNumber)
    val workNumber: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.workNumber)
    val emailAddress: LiveData<String> = MutableLiveData(accountRepository.getAccount().value?.emailAddress)

    val navigateToPersonalInfoEvent: EventLiveData<Unit> = MutableLiveData()

    fun onBiometricChange(boolean: Boolean) {
        biometricStatus.latestValue = boolean
    }

    fun onServiceCallsAndTextsChange(boolean: Boolean) {
        serviceCallsAndTextStatus.latestValue = boolean
    }

    fun onMarketingEmailsChange(boolean: Boolean) {
        marketingEmailStatus.latestValue = boolean
    }

    fun onMarketingCallsAndTextsChange(boolean: Boolean) {
        marketingCallsAndTextStatus.latestValue = boolean
    }

    fun onPersonalInfoClick() {
        navigateToPersonalInfoEvent.emit(Unit)
    }
}
