package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.utility.CoroutineContextProvider
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.TestCoroutineRule
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonalInfoViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: PersonalInfoViewModel
    @MockK
    private lateinit var mockUserRepository: UserRepository

    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    private val coroutineContextProvider: CoroutineContextProvider = CoroutineContextProvider()

    var error: MutableLiveData<Errors> = MutableLiveData()

    @Before
    fun setup() {
        viewModel = PersonalInfoViewModel(
            userRepository = mockUserRepository,
            coroutineContextProvider = coroutineContextProvider
        )
    }

    @Test
    fun onValidateInput_EmptyValueInput() {
        viewModel.onPhoneNumberChanged("")
        viewModel.onConfirmPasswordValueChanged("")
        viewModel.onPasswordValueChanged("")
        error.value = viewModel.validateInput()
        Assert.assertEquals("Phone Number Empty", error.value!!.contains("mobileNumberError"), true)
        Assert.assertEquals("Password Empty", error.value!!.contains("passwordError"), true)
        Assert.assertEquals(
            "Confirm Password Empty",
            error.value!!.contains("confirmPasswordError"),
            true
        )
        Assert.assertEquals("Fields Empty", error.value!!.contains("fieldMandatory"), true)
    }

    @Test
    fun onValidateInput_ValidInputValue() {
        viewModel.onPhoneNumberChanged("890890890")
        viewModel.onConfirmPasswordValueChanged("abc@1234")
        viewModel.onPasswordValueChanged("abc@1234")
        error.value = viewModel.validateInput()
        Assert.assertEquals(
            "Phone Number Empty",
            error.value!!.contains("mobileNumberError"),
            false
        )
        Assert.assertEquals("Password Empty", error.value!!.contains("passwordError"), false)
        Assert.assertEquals(
            "Confirm Password Empty",
            error.value!!.contains("confirmPasswordError"),
            false
        )
        Assert.assertEquals(
            "Password Mismatch",
            error.value!!.contains("passwordMismatchError"),
            false
        )
        Assert.assertEquals("Fields Empty", error.value!!.contains("fieldMandatory"), false)
    }

    @Test
    fun onValidateInput_PasswordAndConfirmPasswordMismatch() {
        viewModel.onPhoneNumberChanged("890890890")
        viewModel.onConfirmPasswordValueChanged("abc@1234")
        viewModel.onPasswordValueChanged("abc@5678")
        error.value = viewModel.validateInput()
        Assert.assertEquals(
            "Password Mismatch",
            error.value!!.contains("passwordMismatchError"),
            true
        )
    }

    /*
    * Added test method to check coroutine methods
    * */
    @Test
    fun onDoneClick_correctPasswordDetailsSubmit_navigateToAccountScreen() =
        runBlockingTest {
            launch {
                viewModel.callUpdatePasswordApi()
            }
        }
}