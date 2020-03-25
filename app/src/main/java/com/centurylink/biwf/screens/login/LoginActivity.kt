package com.centurylink.biwf.screens.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProviders
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initTextChangeListeners()
        initOnClicks()

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

    private fun initOnClicks() {
        binding.loginButton.setOnClickListener { viewModel.onLoginClicked() }
        binding.loginForgotPassword.setOnClickListener { viewModel.onForgotPasswordClicked() }
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
