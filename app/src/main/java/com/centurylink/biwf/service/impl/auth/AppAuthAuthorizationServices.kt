package com.centurylink.biwf.service.impl.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.centurylink.biwf.R
import com.centurylink.biwf.service.auth.AuthResponseType
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceConfig
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.service.auth.TokenStorage
import com.centurylink.biwf.service.auth.createPolicyParam
import com.centurylink.biwf.service.auth.updateAndCommit
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val NEW_POLICY = "newPolicy"

class AppAuthAuthService(
    override val tokenStorage: TokenStorage<AuthState>,
    private val config: AuthServiceConfig,
    private val host: AuthServiceHost
) : AuthService<AuthState> {

    private suspend fun getAuthConfiguration(): AuthorizationServiceConfiguration =
        suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(config.configurationUrl)) { authConfig, error ->
                when {
                    error != null -> cont.resumeWithException(error)
                    authConfig != null -> cont.resume(authConfig)
                    else -> cont.resumeWithException(Error("Should not happen"))
                }
            }
        }

    override suspend fun launchSignInFlow() {
        executeAuthRequest(config.policySignIn, true)
    }

    override suspend fun launchLogoutFlow() {
        tokenStorage.currentPolicy?.let {
            executeAuthRequest(it, false)

            tokenStorage.apply {
                state = null
                currentPolicy = null
            }
        } ?: throw IllegalStateException("There is no auth policy defined.")
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

        val (authorizationResponse, tokenResponse) =
            if (authorizationException == null) {
                androidIntent.parseResponse(config.clientId)
            } else {
                Timber.e(authorizationException)
                AuthorizationResponse.fromIntent(androidIntent) to null
            }

        tokenStorage.state = when {
            authorizationResponse != null && tokenResponse != null ->
                AuthState(authorizationResponse, tokenResponse, null)
            else ->
                AuthState(authorizationResponse, authorizationException)
        }

        return when {
            authorizationException != null -> AuthResponseType.ERROR
            tokenResponse != null -> AuthResponseType.AUTHORIZED
            authorizationResponse != null -> authorizationResponse.handleSuccessfulAuthRedirect()
            else -> throw Error("Should not happen")
        }
    }

    private suspend fun AuthorizationResponse.handleSuccessfulAuthRedirect(): AuthResponseType {
        // A New Policy could be sent if the user switched from login to sign-up
        // from within the auth-flow.
        val newPolicy = additionalParameters[NEW_POLICY]
        if (!newPolicy.isNullOrEmpty()) {
            tokenStorage.currentPolicy = newPolicy
        }

        return with(AuthorizationService(host.hostContext)) {
            try {
                val params = tokenStorage.createPolicyParam() + config.extraParams
                val request = createTokenExchangeRequest(params)
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

    private suspend fun executeAuthRequest(policy: String, login: Boolean) {
        val appContext = host.hostContext.applicationContext

        val customTabsIntent = host.customTabsIntent ?: buildCustomTabsIntent(host.hostContext)

        val authIntent = {
            val defaultIntent = Intent(appContext, AppAuthResponseService::class.java)
            host.getCompletionIntent(appContext) ?: defaultIntent
        }

        val completedAuthorizationIntent = authIntent().setAction(AUTH_COMPLETION_ACTION)

        val cancelledAuthorizationIntent = authIntent().setAction(AUTH_CANCELLATION_ACTION)

        val authRequest = if (login) ::createAuthorizationRequest else ::createLogoutRequest

        tokenStorage.currentPolicy = policy

        with(AuthorizationService(host.hostContext)) {
            try {
                executeAuthRequest(
                    appContext,
                    customTabsIntent,
                    completedAuthorizationIntent,
                    cancelledAuthorizationIntent,
                    authRequest(getAuthConfiguration())
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
            .setDisplay(display)
            .setCodeVerifier(null)
            .setAdditionalParameters(tokenStorage.createPolicyParam() + extraParams)
            .setState(null)
            .build()
    }

    private fun createLogoutRequest(
        authConfig: AuthorizationServiceConfiguration
    ): AuthorizationRequest = with(config) {
        val logoutAuthConfig =
            AuthorizationServiceConfiguration(authConfig.logoutUrl, authConfig.tokenEndpoint)
        return AuthorizationRequest.Builder(
            logoutAuthConfig,
            clientId,
            responseType,
            Uri.parse(authRedirectUrl)
        )
            .setScope(scope)
            .setDisplay(display)
            .setCodeVerifier(null)
            .setAdditionalParameters(tokenStorage.createPolicyParam() + extraParams)
            .setState(null)
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
    private val config: AuthServiceConfig
) : AuthServiceFactory<AuthState> {
    override fun create(host: AuthServiceHost): AuthService<AuthState> =
        AppAuthAuthService(tokenStorage, config, host)
}

private fun Intent.parseResponse(clientId: String) = data!!.let {
    if (it.fragment != null) {
        // Issue; when response-type is "token" the response returns a url with fragment
        // followed by query params.
        val responseJsonString = getStringExtra(AuthorizationResponse.EXTRA_RESPONSE)
        val responseJson = responseJsonString?.let(::JSONObject)
        // Must remove 'scope', otherwise token-refresh-request will fail (invalid 'scope' param).
        val requestJson = responseJson?.getJSONObject("request")?.apply { remove("scope") }
        val authRequest = requestJson?.let(AuthorizationRequest::jsonDeserialize)!!

        val rewrittenUri = it.buildUpon()
            .fragment(null)
            .encodedQuery(it.fragment)
            .build()

        val authResponse = AuthorizationResponse.Builder(authRequest)
            .fromUri(rewrittenUri)
            .build()

        val refreshToken = rewrittenUri.getQueryParameter("refresh_token")

        val tokenRequest =
            TokenRequest.Builder(authRequest.configuration, clientId)
                .setRefreshToken(refreshToken)
                .build()

        val tokenResponse = TokenResponse.Builder(tokenRequest)
            .setAccessToken(authResponse.accessToken)
            .setRefreshToken(refreshToken)
            .build()

        authResponse to tokenResponse
    } else {
        AuthorizationResponse.fromIntent(this)!! to null
    }
}

private fun buildCustomTabsIntent(context: Context): CustomTabsIntent = CustomTabsIntent.Builder()
    .setToolbarColor(ContextCompat.getColor(context, R.color.colorAccent))
    .setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.primary_dark))
    .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    .build()

private val AuthorizationServiceConfiguration.logoutUrl
    get() = Uri.parse(discoveryDoc?.docJson?.getString("end_session_endpoint") ?: "")

private const val AUTH_COMPLETION_ACTION = "com.centurylink.HANDLE_AUTHORIZATION_RESPONSE"
private const val AUTH_CANCELLATION_ACTION = "com.centurylink.HANDLE_AUTHORIZATION_CANCELLATION"
