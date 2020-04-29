package com.centurylink.biwf.screens.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.LoginCoordinator
import com.centurylink.biwf.databinding.ActivityLoginBinding
import com.centurylink.biwf.service.auth.AuthResponseType
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.getViewModel
import javax.inject.Inject

class LoginActivity : BaseActivity(), AuthServiceHost {
    override val hostContext: Context = this

    @Inject
    lateinit var loginCoordinator: LoginCoordinator

    @Inject
    lateinit var viewModelFactory: LoginViewModel.Factory

    private val viewModel by lazy {
        getViewModel<LoginViewModel>(viewModelFactory.withInput(this))
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)

        viewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
        }

        loginCoordinator.navigator.activity = this
        loginCoordinator.observeThis(viewModel.myState)

        initOnClicks()

        handleIntent()
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)

        intent = newIntent
        handleIntent()
    }

    private fun handleIntent() {
        when (val authResult = intent.authResponseType) {
            null -> return

            AuthResponseType.AUTHORIZED -> {
                viewModel.onExistingUserLogin()
                finish()
            }

            else -> {
                displayToast("Error: Got AuthResponseType=$authResult")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loginCoordinator.navigator.activity = null
    }

    private fun initOnClicks() {
        binding.loginButton.setOnClickListener { viewModel.onLoginClicked() }
        binding.loginLearnMore.setOnClickListener { viewModel.onLearnMoreClicked() }
    }

    companion object {
        private const val AUTH_RESPONSE_TYPE = "AuthResponseType"

        private val Intent.authResponseType: AuthResponseType?
            get() = extras?.get(AUTH_RESPONSE_TYPE) as? AuthResponseType

        fun reportLoginResult(context: Context, result: AuthResponseType) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                putExtra(AUTH_RESPONSE_TYPE, result)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        }
    }
}
