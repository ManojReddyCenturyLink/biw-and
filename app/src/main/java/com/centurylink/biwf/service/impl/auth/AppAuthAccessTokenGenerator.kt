package com.centurylink.biwf.service.impl.auth

import android.content.Context
import android.os.Process
import com.centurylink.biwf.service.auth.AccessTokenGenerator
import com.centurylink.biwf.service.auth.TokenStorage
import com.centurylink.biwf.service.auth.createPolicyParam
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import org.json.JSONException
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AppAuthAccessTokenGenerator @Inject constructor(
    private val appContext: Context
) : AccessTokenGenerator {

    internal val tokenIsInvalidated = AtomicBoolean(false)

    override fun generate(tokenStorage: TokenStorage<*>): Single<String> {
        val appAuthTokenStorage = tokenStorage as AppAuthTokenStorage

        val authorizationService = AuthorizationService(appContext)

        val token = Single.create<String> { emitter ->
            // The try-catch prevents a crash when appAuthTokenStorage.state returns an error
            // and then in the catch-block checks if emitter has not been disposed
            try {
                val authState = appAuthTokenStorage.state

                val policyParams = appAuthTokenStorage.createPolicyParam()

                // The 'refreshStrategy' is a method-reference that takes a context (AuthorizationSerivce,
                // AuthState and policy-parameters) and returns the proper method that will do the actual
                // refreshing of the token.
                val refreshStrategy =
                    when {
                        authState?.refreshToken == null -> AuthorizationService::refreshTokenFail
                        tokenIsInvalidated.getAndSet(false) -> AuthorizationService::refreshTokenAlways
                        else -> AuthorizationService::refreshTokenIfNecessary
                    }

                // The 'refreshActionWithCallback' is a function that will do the actual refreshing of the token.
                val refreshActionWithCallback =
                    refreshStrategy(authorizationService, authState!!, policyParams)

                // Now do the actual refresh, where the provided lambda will be called when the refresh succeeds or fails.
                refreshActionWithCallback { accessToken, error ->
                    if (accessToken != null) {
                        Timber.d("Received access token: $accessToken")
                    }
                    if (error != null) {
                        Timber.e(error, "Token refresh error!")
                    }

                    // The AuthState has already been updated when this callback happens. Just assign it.
                    appAuthTokenStorage.state = authState

                    when {
                        error != null -> emitter.onError(error)
                        accessToken != null -> emitter.onSuccess(accessToken)
                        else -> emitter.onError(Error("Should not happen"))
                    }
                }
            } catch (e: JSONException) {
                Timber.e(e)
                if (!emitter.isDisposed) {
                    emitter.onError(Error("Should not happen"))
                }
            }
        }

        return token
            .subscribeOn(SCHEDULER)
            .doFinally { authorizationService.dispose() }
    }

    companion object {
        private val EXECUTOR = Executors.newSingleThreadExecutor { runnable ->
            Thread {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                runnable.run()
            }
        }
        private val SCHEDULER = Schedulers.from(EXECUTOR)
    }
}

/**
 * Use this function to always refresh an access-token, even if the current one is still valid.
 *
 * @return A function that delays the refreshing of the token until the returned function is called.
 */
private fun AuthorizationService.refreshTokenAlways(
    state: AuthState,
    policyParams: Map<String, String>
): ((String?, AuthorizationException?) -> Unit) -> Unit = { callback ->
    val tokenRequest = state.createTokenRefreshRequest(policyParams)

    performTokenRequest(tokenRequest) { resp, error ->
        state.update(resp, error)

        Timber.d("Token callback from refreshTokenAlways()")
        callback(resp?.accessToken, error)
    }
}

/**
 * Use this function to only refresh an access-token when needed (default strategy).
 *
 * @return A function that delays the refreshing of the token until the returned function is called.
 */
private fun AuthorizationService.refreshTokenIfNecessary(
    state: AuthState,
    policyParams: Map<String, String>
): ((String?, AuthorizationException?) -> Unit) -> Unit = { callback ->
    state.performActionWithFreshTokens(this, policyParams) { token, _, error ->
        Timber.d("Token callback from refreshTokenIfNecessary()")
        callback(token, error)
    }
}

@Suppress("UNUSED_PARAMETER", "unused")
/**
 * Use this function to always fail a access-token refresh.
 *
 * @return A function that delays the refreshing of the token until the returned function is called.
 */
private fun AuthorizationService.refreshTokenFail(
    state: AuthState,
    policyParams: Map<String, String>
): ((String?, AuthorizationException?) -> Unit) -> Unit = { callback ->
    Timber.d("Token callback from refreshTokenFail()")
    callback(null, AuthorizationException.AuthorizationRequestErrors.OTHER)
}
