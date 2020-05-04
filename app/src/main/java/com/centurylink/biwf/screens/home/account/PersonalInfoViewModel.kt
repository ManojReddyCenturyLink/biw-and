package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.PersonalInfoCoordinator
import com.centurylink.biwf.model.UserDetails
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class PersonalInfoViewModel @Inject constructor(
) : BaseViewModel() {

    val myState =
        ObservableData(PersonalInfoCoordinator.PersonalInfoCoordinatorDestinations.PROFILE_INFO)
    var passwordVisibility: Boolean = false
    var confirmPasswordVisibility = false
    var error: MutableLiveData<Errors> = MutableLiveData()

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

    fun validateInput(userDetails: UserDetails?): Errors {
        val errors = Errors()
        if (userDetails!!.mobileNumber.isEmpty()) {
            errors["mandatoryFieldError"] = true
            errors["mobileNumberError"] = true
        }
        if (userDetails.password.isEmpty()) {
            errors["mandatoryFieldError"] = true
            errors["passwordError"] = true
        }
        if (userDetails.confirmPassword.isEmpty()) {
            errors["mandatoryFieldError"] = true
            errors["confirmPasswordError"] = true
        }
        if (userDetails.confirmPassword != userDetails.password) {
            errors["passwordMismatchError"] = true
            errors["passwordError"] = true
            errors["confirmPasswordError"] = true
        }
        this.error.value = errors
        return errors
    }

}

