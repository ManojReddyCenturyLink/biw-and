package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.PersonalInfoCoordinator
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class PersonalInfoViewModel @Inject constructor(
) : BaseViewModel() {

    val myState =
        ObservableData(PersonalInfoCoordinator.PersonalInfoCoordinatorDestinations.PROFILE_INFO)
    var error: MutableLiveData<Errors> = MutableLiveData()
    private var passwordVisibility: Boolean = false
    private var confirmPasswordVisibility = false
    private var passwordValue: String = ""
    private var confirmPasswordValue: String = ""
    private var phoneNumberValue: String = ""

    fun updatePassword() {
        myState.value = PersonalInfoCoordinator.PersonalInfoCoordinatorDestinations.DONE
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

    fun onPhoneNumberChanged(phoneNumberValue: String) {
        this.phoneNumberValue = phoneNumberValue
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
        this.error.value = errors
        return errors
    }
}