package com.centurylink.biwf.service.impl.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.centurylink.biwf.R
import com.centurylink.biwf.di.qualifier.ClientType
import com.centurylink.biwf.di.qualifier.HttpClient
import com.centurylink.biwf.service.auth.AuthResponseType
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceConfig
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.service.auth.TokenStorage
import com.centurylink.biwf.service.auth.updateAndCommit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AppAuthAuthService(
    override val tokenStorage: TokenStorage<AuthState>,
    private val config: AuthServiceConfig,
    private val okHttpClient: okhttp3.Call.Factory,
    private val host: AuthServiceHost
) : AuthService<AuthState> {

    private fun getAuthConfiguration(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
            Uri.parse(config.authorizationEndpoint),
            Uri.parse(config.tokenEndpoint)
        )
    }

    override suspend fun launchSignInFlow() {
        executeAuthRequest()
    }

    override suspend fun revokeToken(): Boolean {
        val accessToken = tokenStorage.state?.accessToken
        accessToken ?: return true

        val revokeRequest = Request.Builder()
            .get()
            .url("${config.revokeTokenEndpoint}?client_id=${config.clientId}&token=$accessToken")
            .build()

        val revokeResponse = withContext(Dispatchers.IO) {
            try {
                okHttpClient.newCall(revokeRequest).execute()
            } catch (e: Throwable) {
                Timber.e(e)
                null
            }
        }

        return if (revokeResponse?.isSuccessful == true) {
            clearState()
            true
        } else {
            false
        }
    }

    private fun clearState() {
        tokenStorage.state = null
    }

    override suspend fun handleResponse(androidIntent: Intent): AuthResponseType {
        val action = androidIntent.action
        val redirectUrl = androidIntent.dataString

        return when {
            action == AUTH_CANCELLATION_ACTION -> AuthResponseType.CANCELLED
            action != AUTH_COMPLETION_ACTION -> throw Exception(action ?: "")
            redirectUrl!!.startsWith(config.authRedirectUrl) -> handleAuthRedirect(androidIntent)
            else -> AuthResponseType.UNKNOWN_RESPONSE
        }
    }

    private suspend fun handleAuthRedirect(androidIntent: Intent): AuthResponseType {
        val authorizationException = AuthorizationException.fromIntent(androidIntent)
        val authorizationResponse = AuthorizationResponse.fromIntent(androidIntent)
        val tokenExists = !tokenStorage.state?.accessToken.isNullOrBlank()

        return when {
            authorizationException != null -> AuthResponseType.ERROR
            tokenExists -> AuthResponseType.AUTHORIZED
            authorizationResponse != null -> {
                tokenStorage.state = AuthState(authorizationResponse, authorizationException)
                authorizationResponse.handleSuccessfulAuthRedirect()
            }
            else -> throw Error("Should not happen")
        }
    }

    private suspend fun AuthorizationResponse.handleSuccessfulAuthRedirect(): AuthResponseType {
        return with(AuthorizationService(host.hostContext)) {
            try {
                val request = createTokenExchangeRequest()
                executeTokenRequest(request)
                AuthResponseType.AUTHORIZED
            } catch (e: Throwable) {
                Timber.e(e)
                AuthResponseType.ERROR
            } finally {
                dispose()
            }
        }
    }

    private fun executeAuthRequest() {
        val appContext = host.hostContext.applicationContext

        val customTabsIntent = host.customTabsIntent ?: buildCustomTabsIntent(host.hostContext)

        val authIntent = {
            val defaultIntent = Intent(appContext, AppAuthResponseService::class.java)
            host.getCompletionIntent(appContext) ?: defaultIntent
        }

        val completedAuthorizationIntent = authIntent().setAction(AUTH_COMPLETION_ACTION)

        val cancelledAuthorizationIntent = authIntent().setAction(AUTH_CANCELLATION_ACTION)

        with(AuthorizationService(host.hostContext)) {
            try {
                executeAuthRequest(
                    appContext,
                    customTabsIntent,
                    completedAuthorizationIntent,
                    cancelledAuthorizationIntent,
                    createAuthorizationRequest(getAuthConfiguration())
                )
            } finally {
                dispose()
            }
        }
    }

    private fun createAuthorizationRequest(
        authConfig: AuthorizationServiceConfiguration
    ): AuthorizationRequest = with(config) {
        AuthorizationRequest.Builder(
            authConfig,
            clientId,
            responseType,
            Uri.parse(authRedirectUrl)
        )
            .setScope(scope)
            .build()
    }

    private fun AuthorizationService.executeAuthRequest(
        appContext: Context,
        customTabsIntent: CustomTabsIntent,
        completedAuthorizationIntent: Intent,
        cancelledAuthorizationIntent: Intent,
        authRequest: AuthorizationRequest
    ) {
        val pendingCompletionIntent =
            PendingIntent.getService(
                appContext,
                authRequest.hashCode(),
                completedAuthorizationIntent,
                0
            )

        val pendingCancelledIntent =
            PendingIntent.getService(
                appContext,
                authRequest.hashCode(),
                cancelledAuthorizationIntent,
                0
            )

        performAuthorizationRequest(
            authRequest,
            pendingCompletionIntent,
            pendingCancelledIntent,
            customTabsIntent
        )
    }

    private suspend fun AuthorizationService.executeTokenRequest(
        tokenRequest: TokenRequest
    ): TokenResponse = suspendCancellableCoroutine { cont ->
        performTokenRequest(tokenRequest) { tokenResponse, error ->
            tokenStorage.updateAndCommit { update(tokenResponse, error) }

            when {
                error != null -> cont.resumeWithException(error)
                tokenResponse != null -> cont.resume(tokenResponse)
                else -> cont.resumeWithException(Error("Should not happen"))
            }
        }
    }
}

class AppAuthAuthServiceFactory @Inject constructor(
    private val tokenStorage: TokenStorage<AuthState>,
    private val config: AuthServiceConfig,
    @HttpClient(ClientType.NONE) private val okHttpClient: okhttp3.Call.Factory
) : AuthServiceFactory<AuthState> {
    override fun create(host: AuthServiceHost): AuthService<AuthState> =
        AppAuthAuthService(tokenStorage, config, okHttpClient, host)
}

private fun buildCustomTabsIntent(context: Context): CustomTabsIntent = CustomTabsIntent.Builder()
    .setToolbarColor(ContextCompat.getColor(context, R.color.colorAccent))
    .setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.primary_dark))
    .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    .build()

private const val AUTH_COMPLETION_ACTION = "com.centurylink.HANDLE_AUTHORIZATION_RESPONSE"
private const val AUTH_CANCELLATION_ACTION = "com.centurylink.HANDLE_AUTHORIZATION_CANCELLATION"
