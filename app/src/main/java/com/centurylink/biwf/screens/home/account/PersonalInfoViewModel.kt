package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.PersonalInfoCoordinator
import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.network.UserService
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.MutableStateFlow
import com.centurylink.biwf.utility.ObservableData
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PersonalInfoViewModel @Inject constructor(
    private val userService: UserService,
    private val sharedPreferences: Preferences
) : BaseViewModel() {

    val myState =
        ObservableData(PersonalInfoCoordinator.PersonalInfoCoordinatorDestinations.PROFILE_INFO)
    var error: MutableLiveData<Errors> = MutableLiveData()
    private var passwordVisibility: Boolean = false
    private var confirmPasswordVisibility = false
    private var passwordValue: String = ""
    private var confirmPasswordValue: String = ""
    private var phoneNumberValue: String = ""

    val userPasswordFlow: Flow<Unit> = MutableStateFlow()
    val userPasswordErrorFlow: Flow<Throwable> = MutableStateFlow()

    fun updatePassword() {
        callUpdatePasswordApi(passwordValue)
        myState.value = PersonalInfoCoordinator.PersonalInfoCoordinatorDestinations.DONE
    }

    private fun callUpdatePasswordApi(password: String) {
        val userId = sharedPreferences.getUserId(Preferences.USER_ID)
        viewModelScope.launch {
            try {
                userPasswordFlow.latestValue =
                    userService.updatePassword(userId!!, UpdatedPassword(password))
            } catch (e: Throwable) {
                userPasswordErrorFlow.latestValue = e
            }
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