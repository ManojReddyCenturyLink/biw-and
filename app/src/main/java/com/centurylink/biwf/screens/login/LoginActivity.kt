package com.centurylink.biwf.screens.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.R
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
                biometricCheck()
            }
        }

        viewModel.myState.observeWith(loginCoordinator)
        initOnClicks()
        handleIntent()
    }

    private fun biometricCheck() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBioDialog()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            }
        }
    }

    private fun showBioDialog() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                    showBioDialog()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_message))
            .setNegativeButtonText(getString(R.string.cancel))
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
                viewModel.onExistingUserLogin()
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
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

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