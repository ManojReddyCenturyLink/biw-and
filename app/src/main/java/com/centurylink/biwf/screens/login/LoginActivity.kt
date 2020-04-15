package com.centurylink.biwf.screens.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.LoginCoordinator
import com.centurylink.biwf.databinding.ActivityLoginBinding
import com.centurylink.biwf.repos.AccountRepositoryImpl
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var loginCoordinator: LoginCoordinator
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)

        viewModel = LoginViewModel(AccountRepositoryImpl())
        viewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
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
        binding.loginButton.setOnClickListener { viewModel.onLoginClicked() }
        binding.loginForgotPassword.setOnClickListener { viewModel.onForgotPasswordClicked() }
        binding.loginLearnMore.setOnClickListener { viewModel.onLearnMoreClicked() }
        binding.loginRememberMeCheckbox.setOnCheckedChangeListener { _, boolean ->
            viewModel.onRememberMeCheckChanged(boolean)
        }
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
