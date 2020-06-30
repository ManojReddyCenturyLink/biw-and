package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonalInfoViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: PersonalInfoViewModel
    @MockK
    private lateinit var mockUserRepository: UserRepository

    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    var error: MutableLiveData<Errors> = MutableLiveData()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = PersonalInfoViewModel(
            userRepository = mockUserRepository
        )
    }

    @Test
    fun onValidateInput_EmptyValueInput() {
        viewModel.onPhoneNumberChanged("")
        viewModel.onConfirmPasswordValueChanged("")
        viewModel.onPasswordValueChanged("")
        error.value = viewModel.validateInput()
        assertThat("Phone Number Empty", error.value!!.contains("mobileNumberError"))
        assertThat("Password Empty", error.value!!.contains("passwordError"))
        assertThat("Confirm Password Empty", error.value!!.contains("confirmPasswordError"))
        assertThat("Fields Empty", error.value!!.contains("fieldMandatory"))
    }

    @Test
    fun onValidateInput_ValidInputValue() {
        viewModel.onPhoneNumberChanged("890890890")
        viewModel.onConfirmPasswordValueChanged("abc@1234")
        viewModel.onPasswordValueChanged("abc@1234")
        error.value = viewModel.validateInput()
        assertThat("Phone Number Empty Check", !error.value!!.contains("mobileNumberError"))
        assertThat("Password Empty Check", !error.value!!.contains("passwordError"))
        assertThat("Confirm Password Empty Check", !error.value!!.contains("confirmPasswordError"))
        assertThat("Password Mismatch Check", !error.value!!.contains("passwordMismatchError"))
        assertThat("Fields Empty Check", !error.value!!.contains("fieldMandatory"))
    }

    @Test
    fun onValidateInput_PasswordAndConfirmPasswordMismatch() {
        viewModel.onPhoneNumberChanged("890890890")
        viewModel.onConfirmPasswordValueChanged("abc@1234")
        viewModel.onPasswordValueChanged("abc@5678")
        error.value = viewModel.validateInput()
        assertThat("Password Mismatch Check", error.value!!.contains("passwordMismatchError"))
    }
}