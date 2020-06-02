package com.centurylink.biwf.screens.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.LoginCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityLoginBinding
import com.centurylink.biwf.service.auth.AuthResponseType
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.getViewModel
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : BaseActivity(), AuthServiceHost {
    override val hostContext: Context = this

    @Inject
    lateinit var loginCoordinator: LoginCoordinator
    @Inject
    lateinit var viewModelFactory: LoginViewModel.Factory
    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        val navFromAccountScreen = intent.getBooleanExtra(NAVIGATED_FROM_ACCOUNT_SCREEN, false)
        getViewModel<LoginViewModel>(viewModelFactory.withInput(this, navFromAccountScreen))
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)
        navigator.observe(this)

        viewModel.apply {
            errorEvents.handleEvent { displayToast(it) }
            showBioMetricsLogin.observe {
                biometricCheck(it)
            }
        }

        viewModel.myState.observeWith(loginCoordinator)
        initOnClicks()
        handleIntent()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun biometricCheck(biometricPrompt: BiometricPromptMessage) {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBioDialog(biometricPrompt)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            }
        }
    }

    private fun showBioDialog(biometricMessage: BiometricPromptMessage) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.d("Error  -- $errString")
                    showBioDialog(biometricMessage)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onLoginSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.d("Authentication Failed")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(biometricMessage.title))
            .setSubtitle(getString(biometricMessage.subTitle))
            .setNegativeButtonText(getString(biometricMessage.negativeText))
            .build()

        biometricPrompt.authenticate(promptInfo)
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
                viewModel.onLoginSuccess()
                finish()
            }
            else -> {
                Timber.d("Got non-successful AuthResponseType=$authResult")
            }
        }
    }

    private fun initOnClicks() {
        binding.loginButton.setOnClickListener { viewModel.onLoginClicked() }
        binding.loginLearnMore.setOnClickListener { viewModel.onLearnMoreClicked() }
    }

    companion object {
        private const val AUTH_RESPONSE_TYPE = "AuthResponseType"
        private const val NAVIGATED_FROM_ACCOUNT_SCREEN = "navigatedFromAccountScreen"

        fun newIntent(context: Context, boolean: Boolean): Intent {
            return Intent(context, LoginActivity::class.java).apply {
                putExtra(NAVIGATED_FROM_ACCOUNT_SCREEN, boolean)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }

        private val Intent.authResponseType: AuthResponseType?
            get() = extras?.get(AUTH_RESPONSE_TYPE) as? AuthResponseType

        fun reportLoginResult(context: Context, result: AuthResponseType) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                putExtra(AUTH_RESPONSE_TYPE, result)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }
}