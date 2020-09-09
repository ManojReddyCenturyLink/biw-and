package com.centurylink.biwf.screens.home.account

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonalInfoViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: PersonalInfoViewModel

    @MockK
    private lateinit var mockUserRepository: UserRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    var error: MutableLiveData<Errors> = MutableLiveData()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        viewModel = PersonalInfoViewModel(
            userRepository = mockUserRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
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

    @Test
    fun onDoneClick_correctPasswordDetailsSubmit_navigateToAccountScreen() =
        runBlockingTest {
            launch {
                viewModel.callUpdatePasswordApi()
            }
        }

    @Test
    fun onValidateInput_PasswordAndConfirmPasswordGreaterThanTenDigits() {
        viewModel.onPhoneNumberChanged("89089089090000034")
        viewModel.onConfirmPasswordValueChanged("abc@1234")
        viewModel.onPasswordValueChanged("abc@5678")
        error.value = viewModel.validateInput()
        assertThat("Password Mismatch Check", error.value!!.contains("passwordMismatchError"))
    }

    @Test
    fun onValidateInput_PasswordAndConfirmPasswordLessThanSixDigits() {
        viewModel.onPhoneNumberChanged("89089")
        viewModel.onConfirmPasswordValueChanged("abc@1234")
        viewModel.onPasswordValueChanged("abc@5678")
        error.value = viewModel.validateInput()
        assertThat("Password Mismatch Check", error.value!!.contains("passwordMismatchError"))
    }

    @Test
    fun toggleConfirmPasswordVisibility() {
        viewModel.toggleConfirmPasswordVisibility()
        Assert.assertSame(false, viewModel.toggleConfirmPasswordVisibility())
    }


    @Test
    fun togglePasswordVisibility() {
        viewModel.togglePasswordVisibility()
        Assert.assertSame(false, viewModel.togglePasswordVisibility())
    }

    @Test
    fun analyticsManagerInterface_handle() {
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logResetPasswordSuccess()
        viewModel.logResetPasswordFailure()
        viewModel.logUpdateEmailPopupClick()
        viewModel.onConfirmPasswordValueChanged("test")
    }
}