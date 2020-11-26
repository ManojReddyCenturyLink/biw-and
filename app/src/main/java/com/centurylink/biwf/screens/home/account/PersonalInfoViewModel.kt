package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.PersonalInfoCoordinatorDestinations
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Personal info view model
 *
 * @property userRepository - repository instance to handle user api calls
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class PersonalInfoViewModel @Inject constructor(
    private val userRepository: UserRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    val myState = EventFlow<PersonalInfoCoordinatorDestinations>()
    var error = EventFlow<Errors>()
    var userPasswordFlow = EventFlow<String>()
    private var passwordVisibility: Boolean = false
    private var confirmPasswordVisibility = false
    private var passwordValue: String = ""
    private var confirmPasswordValue: String = ""
    private var phoneNumberValue: String = ""

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_PERSONAL_INFO)
    }

    /**
     * Call update password api - update account's password by calling Api
     *
     */
    fun callUpdatePasswordApi() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_PERSONAL_INFO)
        viewModelScope.launch {
            val res = userRepository.resetPassWord(passwordValue)
            userPasswordFlow.latestValue = res
        }
    }

    /**
     * Toggle password visibility - It will handle toggling password visibility logic
     *
     * @return - negates the password visibility
     */
    fun togglePasswordVisibility(): Boolean {
        passwordVisibility = !passwordVisibility
        return passwordVisibility
    }

    /**
     * Toggle confirm password visibility - It will handle toggling confirm password visibility
     * logic
     *
     * @return - negates the confirm password visibility
     */
    fun toggleConfirmPasswordVisibility(): Boolean {
        confirmPasswordVisibility = !confirmPasswordVisibility
        return confirmPasswordVisibility
    }

    /**
     * On password value changed
     *
     * @param passwordValue - The password value to be updated
     */
    fun onPasswordValueChanged(passwordValue: String) {
        this.passwordValue = passwordValue
    }

    /**
     * Log reset password success
     *
     */
    fun logResetPasswordSuccess() {
        analyticsManagerInterface.logApiCall(AnalyticsKeys.RESET_PASSWORD_SUCCESS)
    }

    /**
     * Log reset password failure
     *
     */
    fun logResetPasswordFailure() {
        analyticsManagerInterface.logApiCall(AnalyticsKeys.RESET_PASSWORD_FAILURE)
    }

    /**
     * Log update email popup click
     *
     */
    fun logUpdateEmailPopupClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_UPDATE_EMAIL_INFO)
    }

    /**
     * On confirm password value changed
     *
     * @param confirmPasswordValue - The value that sets confirm password value
     */
    fun onConfirmPasswordValueChanged(confirmPasswordValue: String) {
        this.confirmPasswordValue = confirmPasswordValue
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

    /**
     * Validate input - It will validate inputs for personal info screen
     *
     * @return - returns error by observing screen input views
     */
    fun validateInput(): Errors {
        val errors = Errors()
        if (phoneNumberValue.isEmpty()) {
            errors["mobileNumberError"] = "mobileNumberError"
            errors["fieldMandatory"] = "fieldMandatory"
        }
        if (phoneNumberValue.length <12 && phoneNumberValue.isNotEmpty()) {
            errors["mobileNumberLengthError"] = "mobileNumberLengthError"
            errors["mobileNumberLength"] = "mobileNumberLength"
        }
        if (passwordValue.isEmpty()) {
            errors["passwordError"] = "passwordError"
            errors["fieldMandatory"] = "fieldMandatory"
        }
        if (confirmPasswordValue.isEmpty()) {
            errors["confirmPasswordError"] = "confirmPasswordError"
            errors["fieldMandatory"] = "fieldMandatory"
        }
        if (confirmPasswordValue != passwordValue && confirmPasswordValue.isNotEmpty() && passwordValue.isNotEmpty()) {
            errors["passwordMismatchError"] = "passwordMismatchError"
            errors["passwordError"] = "passwordError"
            errors["confirmPasswordError"] = "confirmPasswordError"
        }
        this.error.latestValue = errors
        return errors
    }
}
