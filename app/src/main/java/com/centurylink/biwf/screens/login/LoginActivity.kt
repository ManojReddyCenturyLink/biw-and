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

/**
 * Login activity - This class handle common methods related to login screen
 *
 * @constructor Create empty Login activity
 */
class LoginActivity : BaseActivity(), AuthServiceHost {
    override val hostContext: Context = this

    @Inject
    lateinit var loginCoordinator: LoginCoordinator

    @Inject
    lateinit var viewModelFactory: LoginViewModel.Factory

    @Inject
    lateinit var navigator: Navigator

    override val viewModel by lazy {
        getViewModel<LoginViewModel>(viewModelFactory.withInput(this))
    }

    private lateinit var binding: ActivityLoginBinding

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)
        navigator.observe(this)

        viewModel.apply {
            showBioMetricsLogin.observe {
                biometricCheck(it)
            }
        }

        viewModel.myState.observeWith(loginCoordinator)
        handleIntent()
    }

    /**
     * On resume - Called when the fragment is visible to the user and actively running
     *
     */
    override fun onResume() {
        super.onResume()
        viewModel.handleSignInFlow()
    }

    /**
     * On back pressed - This will handle back key click listeners
     *
     */
    override fun onBackPressed() {
        finishAffinity()
    }

    /**
     * Biometric check - It will check for hardware related issues to activate biometrics
     *
     * @param biometricPrompt - The message prompt to be displayed
     */
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

    /**
     * Show bio dialog - It shows the biometric alert dialog based on biometric message to displayed
     *
     * @param biometricMessage - The message prompt to be displayed
     */
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
                    viewModel.onBiometricFailure()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricSuccess()
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

    /**
     * On new intent - It will be called with the starting Intent being passed as the
     * intent argument.
     *
     * @param newIntent - The intent to be passed
     */
    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        intent = newIntent
        handleIntent()
    }

    /**
     * Handle intent
     *
     */
   private fun handleIntent() {
        when (val authResult = intent.authResponseType) {
            null -> return

            AuthResponseType.AUTHORIZED -> {
                viewModel.onLoginSuccess()
                finish()
            }
            AuthResponseType.CANCELLED -> {
                Timber.d("User cancelled login attempt")
                finish()
            }
            else -> {
                Timber.d("Got non-successful AuthResponseType=$authResult")
                finish()
            }
        }
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        private const val AUTH_RESPONSE_TYPE = "AuthResponseType"

        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        private val Intent.authResponseType: AuthResponseType?
            get() = extras?.get(AUTH_RESPONSE_TYPE) as? AuthResponseType

        fun reportLoginResult(context: Context, result: AuthResponseType) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                putExtra(AUTH_RESPONSE_TYPE, result)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}