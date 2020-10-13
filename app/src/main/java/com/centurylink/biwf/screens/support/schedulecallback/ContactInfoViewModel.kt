package com.centurylink.biwf.screens.support.schedulecallback

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ContactInfoCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.PhoneNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactInfoViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    private val accountRepository: AccountRepository,
    analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val myState = EventFlow<ContactInfoCoordinatorDestinations>()
    val accountDetailsInfo: Flow<UiAccountDetails> = BehaviorStateFlow()
    var error = EventFlow<Errors>()
    private var phoneNumberValue: String = ""
    var userId: String = ""
    var progressViewFlow = EventFlow<Boolean>()
    var errorMessageFlow = EventFlow<String>()
    var isExistingUserWithPhoneNumberState= EventFlow<Boolean>()
    var uiAccountDetails: UiAccountDetails = UiAccountDetails()

    init {
        initAccountAndContactApiCalls()
    }

    fun launchSelectTime(customerCareOption: String, additionalInfo: String, phoneNumber: String, userId: String) {
        val bundle = Bundle()
        bundle.putString(SelectTimeActivity.SELECT_TIME, customerCareOption)
        bundle.putString(SelectTimeActivity.ADDITIONAL_INFO, additionalInfo)
        bundle.putString(SelectTimeActivity.PHONE_NUMBER, phoneNumber)
        bundle.putString(SelectTimeActivity.USER_ID, userId)
        ContactInfoCoordinatorDestinations.bundle = bundle
        myState.latestValue = ContactInfoCoordinatorDestinations.SELECT_TIME
    }

    fun initAccountAndContactApiCalls() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestContactDetails()
            requestAccountDetails()
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun requestAccountDetails() {
        val userAccountDetails = accountRepository.getAccountDetails()
        userAccountDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_SUCCESS)
            userId = it.Id
        }
    }


    private suspend fun requestContactDetails() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_SUCCESS)
            updateUIContactDetailsFromAccounts(it)
        }
    }


    private fun updateUIContactDetailsFromAccounts(accountDetails: AccountDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            cellPhone = PhoneNumber(accountDetails.phone ?: "").toString())
        isExistingUserWithPhoneNumberState.latestValue = !uiAccountDetails.cellPhone.isNullOrEmpty()
        accountDetailsInfo.latestValue = uiAccountDetails
    }

    fun validateInput(): Errors {
        val errors = Errors()
        if (phoneNumberValue.isEmpty() || phoneNumberValue.length < mobileMinLength ) {
            errors["mobileNumberError"] = "mobileNumberError"
        }
        this.error.latestValue = errors
        return errors
    }

    fun onPhoneNumberChanged(phoneNumberValue: String): String {
        val digits = StringBuilder()
        val phone = StringBuilder()
        val chars: CharArray = phoneNumberValue.toCharArray()
        for (x in chars.indices) {
            if (Character.isDigit(chars[x])) {
                digits.append(chars[x])
            }
        }
        if (digits.toString().length > 3) {
            phone.append(digits.toString().substring(0, 3) + "-")
            if (digits.toString().length > 6) {
                phone.append(digits.toString().substring(3, 6) + "-")
                /** the phone number will not go over 12 digits  if ten, set the limit to ten digits */
                if (digits.toString().length >= 10) {
                    phone.append(digits.toString().substring(6, 10))
                } else {
                    phone.append(digits.toString().substring(6))
                }
            } else {
                phone.append(digits.toString().substring(3))
            }
        } else {
            this.phoneNumberValue = digits.toString()
            return digits.toString()
        }
        this.phoneNumberValue = phone.toString()
        return this.phoneNumberValue
    }

    data class UiAccountDetails(
        val cellPhone: String? = null
    )

    companion object {
        const val mobileMinLength = 12
    }
}