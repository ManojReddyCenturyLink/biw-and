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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import org.json.JSONObject
import javax.inject.Inject

private const val NEW_POLICY = "newPolicy"

class AppAuthAuthService(
    override val tokenStorage: TokenStorage<AuthState>,
    private val config: AuthServiceConfig,
    private val host: AuthServiceHost
) : AuthService<AuthState> {

    private val authConfiguration
        get() = Single.create<AuthorizationServiceConfiguration> { emitter ->
            val result = runCatching {
                AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(config.configurationUrl)) { authConfig, error ->
                    when {
                        error != null -> emitter.onError(error)
                        authConfig != null -> emitter.onSuccess(authConfig)
                        else -> emitter.onError(Error("Should not happen"))
                    }
                }
            }
            result.getOrElse { emitter.onError(it) }
        }

    override fun launchSignInFlow(): Completable = executeAuthRequest(config.policySignIn, true)

    override fun launchLogoutFlow(): Completable = tokenStorage.currentPolicy?.let {
        executeAuthRequest(it, false)
            .doOnComplete {
                tokenStorage.apply {
                    state = null
                    currentPolicy = null
                }
            }
    } ?: Completable.error(IllegalStateException("There is no auth policy defined."))

    override fun handleResponse(androidIntent: Intent): Single<AuthResponseType> {
        val action = androidIntent.action
        val redirectUrl = androidIntent.dataString

        return when {
            action == AUTH_CANCELLATION_ACTION -> Single.just(AuthResponseType.CANCELLED)
            action != AUTH_COMPLETION_ACTION -> Single.error(Exception(action ?: ""))
            redirectUrl!!.startsWith(config.authRedirectUrl) -> handleAuthRedirect(androidIntent)
            else -> Single.just(AuthResponseType.UNKNOWN_RESPONSE)
        }
    }

    private fun handleAuthRedirect(androidIntent: Intent): Single<AuthResponseType> {
        val authorizationException = AuthorizationException.fromIntent(androidIntent)

        val (authorizationResponse, tokenResponse) =
            if (authorizationException == null) {
                androidIntent.parseResponse(config.clientId)
            } else {
                AuthorizationResponse.fromIntent(androidIntent) to null
            }

        tokenStorage.state =
            if (authorizationResponse != null) AuthState(authorizationResponse, tokenResponse, null)
            else AuthState(null, authorizationException)

        return when {
            authorizationException != null -> Single.error(authorizationException)
            tokenResponse != null -> Single.just(AuthResponseType.AUTHORIZED)
            authorizationResponse != null -> handleSuccessfulAuthRedirect(authorizationResponse)
            else -> Single.error(Error("Should not happen"))
        }
    }

    private fun handleSuccessfulAuthRedirect(authorizationResponse: AuthorizationResponse): Single<AuthResponseType> {
        val authorizationService = AuthorizationService(host.hostContext)

        // A New Policy could be sent if the user switched from login to sign-up
        // from within the auth-flow.
        val newPolicy = authorizationResponse.additionalParameters[NEW_POLICY]
        if (!newPolicy.isNullOrEmpty()) {
            tokenStorage.currentPolicy = newPolicy
        }

        return Single.just(authorizationResponse)
            .map { it.createTokenExchangeRequest(tokenStorage.createPolicyParam() + config.extraParams) }
            .flatMap { authorizationService.executeTokenRequest(it) }
            .map { AuthResponseType.AUTHORIZED }
            .doFinally { authorizationService.dispose() }
    }

    private fun executeAuthRequest(policy: String, login: Boolean): Completable {
        val appContext = host.hostContext.applicationContext

        val customTabsIntent = host.customTabsIntent ?: buildCustomTabsIntent(host.hostContext)

        val authIntent = {
            val defaultIntent = Intent(appContext, AppAuthResponseService::class.java)
            host.getCompletionIntent(appContext) ?: defaultIntent
        }

        val completedAuthorizationIntent = authIntent().setAction(AUTH_COMPLETION_ACTION)

        val cancelledAuthorizationIntent = authIntent().setAction(AUTH_CANCELLATION_ACTION)

        val authorizationService = AuthorizationService(host.hostContext)

        tokenStorage.currentPolicy = policy

        return authConfiguration
            .map {
                if (login) createAuthorizationRequest(it)
                else createLogoutRequest(it)
            }
            .flatMapCompletable {
                authorizationService.executeAuthRequest(
                    appContext,
                    customTabsIntent,
                    completedAuthorizationIntent,
                    cancelledAuthorizationIntent,
                    it
                )
            }
            .doFinally { authorizationService.dispose() }
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
    ): Completable {
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

        val result = runCatching {
            performAuthorizationRequest(
                authRequest,
                pendingCompletionIntent,
                pendingCancelledIntent,
                customTabsIntent
            )
        }

        return result.fold(
            onFailure = { Completable.error(it) },
            onSuccess = { Completable.complete() }
        )
    }

    private fun AuthorizationService.executeTokenRequest(
        tokenRequest: TokenRequest
    ) = Single.create<TokenResponse> { emitter ->
        performTokenRequest(tokenRequest) { tokenResponse, error ->
            tokenStorage.updateAndCommit { update(tokenResponse, error) }

            when {
                error != null -> emitter.onError(error)
                tokenResponse != null -> emitter.onSuccess(tokenResponse)
                else -> emitter.onError(Error("Should not happen"))
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
