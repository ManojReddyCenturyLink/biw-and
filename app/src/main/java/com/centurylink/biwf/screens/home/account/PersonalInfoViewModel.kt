package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.PersonalInfoCoordinatorDestinations
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PersonalInfoViewModel @Inject constructor(
    private val userRepository: UserRepository,
    modemRebootMonitorService: ModemRebootMonitorService
) : BaseViewModel(modemRebootMonitorService) {

    val myState = EventFlow<PersonalInfoCoordinatorDestinations>()
    var error = EventFlow<Errors>()
    var userPasswordFlow = EventFlow<String>()
    private var passwordVisibility: Boolean = false
    private var confirmPasswordVisibility = false
    private var passwordValue: String = ""
    private var confirmPasswordValue: String = ""
    private var phoneNumberValue: String = ""

    fun callUpdatePasswordApi() {
        viewModelScope.launch {
            val res = userRepository.resetPassWord(passwordValue)
            userPasswordFlow.latestValue = res
        }
    }

    fun togglePasswordVisibility(): Boolean {
        passwordVisibility = !passwordVisibility
        return passwordVisibility
    }

    fun toggleConfirmPasswordVisibility(): Boolean {
        confirmPasswordVisibility = !confirmPasswordVisibility
        return confirmPasswordVisibility
    }

    fun onPasswordValueChanged(passwordValue: String) {
        this.passwordValue = passwordValue
    }

    fun onConfirmPasswordValueChanged(confirmPasswordValue: String) {
        this.confirmPasswordValue = confirmPasswordValue
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

    fun validateInput(): Errors {
        val errors = Errors()
        if (phoneNumberValue.isEmpty()) {
            errors["mobileNumberError"] = "mobileNumberError"
            errors["fieldMandatory"] = "fieldMandatory"
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
