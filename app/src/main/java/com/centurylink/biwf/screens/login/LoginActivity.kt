package com.centurylink.biwf.screens.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.LoginCoordinator
import com.centurylink.biwf.databinding.ActivityLoginBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    companion object{
        val USER_ID = "USER_ID"
    }
    @Inject
    lateinit var loginCoordinator: LoginCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    private val viewModel by lazy { ViewModelProvider(this, factory).get(LoginViewModel::class.java) }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)

        viewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
            userId.bindToTextView(binding.loginEmailInput)
            checkRememberMe.bindToCheckBox(binding.loginRememberMeCheckbox)
        }

        loginCoordinator.navigator.activity = this
        loginCoordinator.observeThis(viewModel.myState)

        initTextChangeListeners()
        initOnClicks()
    }

    override fun onDestroy() {
        super.onDestroy()
        loginCoordinator.navigator.activity = null
    }

    private fun initOnClicks() {
        binding.loginCardTitle.setOnClickListener { viewModel.onExistingUserLogin() }
        binding.loginButton.setOnClickListener { viewModel.onLoginClicked()
            /*Adding here for testing purpose, method call will get move to onLoginSuccess after api implementation*/
            viewModel.onRememberMeCheckChanged(binding.loginRememberMeCheckbox.isChecked, binding.loginEmailInput.text.toString())
        }
        binding.loginForgotPassword.setOnClickListener { viewModel.onForgotPasswordClicked() }
        binding.loginLearnMore.setOnClickListener { viewModel.onLearnMoreClicked() }
    }

    private fun initTextChangeListeners() {
        binding.loginEmailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editText: Editable?) {
                viewModel.onEmailTextChanged(editText.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }
        })
        binding.loginPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editText: Editable?) {
                viewModel.onPasswordTextChanged(editText.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }
        })
    }
}