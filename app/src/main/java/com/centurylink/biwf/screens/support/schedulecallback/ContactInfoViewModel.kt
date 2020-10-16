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
import javax.inject.Inject

/**
 * Contact info view model
 *
 * @property accountRepository
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class ContactInfoViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    private val accountRepository: AccountRepository,
    analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val myState = EventFlow<ContactInfoCoordinatorDestinations>()
    val accountDetailsInfo: Flow<UiAccountDetails> = BehaviorStateFlow()
    var error = EventFlow<Errors>()
    private var phoneNumberValue: String = ""
    var progressViewFlow = EventFlow<Boolean>()
    var errorMessageFlow = EventFlow<String>()
    var isExistingUserWithPhoneNumberState = EventFlow<Boolean>()
    var isExistingUserWithPhoneNumber = false
    var uiAccountDetails: UiAccountDetails = UiAccountDetails()

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        isExistingUserWithPhoneNumberState.latestValue = false
        initContactApiCall()
    }

    /**
     * Launch select time - It will launch contact info screen from select screen by passing
     * bundle
     *
     * @param customerCareOption - The option selected from list of customer care options
     * @param additionalInfo - The additional information to be added
     * @param phoneNumber - The selected phone number
     */
    fun launchSelectTime(customerCareOption: String, additionalInfo: String, phoneNumber: String) {
        val bundle = Bundle()
        bundle.putString(SelectTimeActivity.SELECT_TIME, customerCareOption)
        bundle.putString(SelectTimeActivity.ADDITIONAL_INFO, additionalInfo)
        bundle.putString(SelectTimeActivity.PHONE_NUMBER, phoneNumber)
        ContactInfoCoordinatorDestinations.bundle = bundle
        myState.latestValue = ContactInfoCoordinatorDestinations.SELECT_TIME
    }

    /**
     * Init contact api call - It will initializes contact apis
     *
     */
    private fun initContactApiCall() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestContactDetails()
            progressViewFlow.latestValue = false
        }
    }

    /**
     * Request contact details - fetches contact details from contact service api
     *
     */
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

    /**
     * Update u i contact details from accounts
     *
     * @param accountDetails - The instance to hold account details
     */
    private fun updateUIContactDetailsFromAccounts(accountDetails: AccountDetails) {
        uiAccountDetails = uiAccountDetails.copy(
            cellPhone = PhoneNumber(accountDetails.phone ?: "").toString())
        this.isExistingUserWithPhoneNumber = !uiAccountDetails.cellPhone.isNullOrEmpty()
        this.isExistingUserWithPhoneNumberState.latestValue = !uiAccountDetails.cellPhone.isNullOrEmpty()
        accountDetailsInfo.latestValue = uiAccountDetails
    }

    /**
     * Validate input - It will validate phone input for contact info screen
     *
     * @return - returns error value if there is an error in phone number
     * returns null if there is no error
     */
    fun validateInput(): Errors {
        val errors = Errors()
        if (phoneNumberValue.isEmpty() || phoneNumberValue.length < mobileMinLength ) {
            errors["mobileNumberError"] = "mobileNumberError"
        }
        this.error.latestValue = errors
        return errors
    }

    /**
     * On phone number changed - format's the phone number on text changes
     *
     * @param phoneNumberValue - The phone number to be watched
     * @return - returns the formatted phone number
     */
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

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        const val mobileMinLength = 12
    }
}