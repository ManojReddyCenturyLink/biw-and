package com.centurylink.biwf.service.impl.auth

import android.content.Context
import com.centurylink.biwf.service.auth.AccessTokenGenerator
import com.centurylink.biwf.service.auth.TokenStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationService
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AppAuthAccessTokenGenerator @Inject constructor(
    private val appContext: Context
) : AccessTokenGenerator {

    internal val tokenIsInvalidated = AtomicBoolean(false)

    /**
     * Use a [Mutex] so that there will only be at most one token-exchange request in the air at
     * any given time. If multiple callers are calling [generate] at the same time, the first one
     * will continue and actually may get a fresh token. The later ones will just get the newly
     * refreshed token.
     */
    private val mutex = Mutex()

    override suspend fun generate(tokenStorage: TokenStorage<*>): String = mutex.withLock {
        val appAuthTokenStorage = tokenStorage as AppAuthTokenStorage

        // The try-catch prevents a crash when appAuthTokenStorage.state returns an error
        // and then in the catch-block checks if emitter has not been disposed
        val authState = appAuthTokenStorage.state

        // The 'refreshStrategy' is a method-reference that takes a context (AuthorizationService,
        // AuthState and policy-parameters) and returns the proper method that will do the actual
        // refreshing of the token.
        val refreshStrategy = when {
            authState?.accessToken == null -> throw AuthorizationRequestErrors.OTHER
            // TODO - we may need to create custom implementations for retrieving a new access
            //  token, since our Apigee implementation might not align with the AppAuth convention.
            //  For this we would revise refreshTokenAlways() and refreshTokenIfNecessry()
            tokenIsInvalidated.getAndSet(false) -> AuthorizationService::refreshTokenAlways
            else -> AuthorizationService::refreshTokenIfNecessary
        }

        return with(AuthorizationService(appContext)) {
            // Now do the actual refresh, where the provided lambda will be called when the refresh succeeds or fails.
            try {
                refreshStrategy(this, authState!!)
            } catch (error: Throwable) {
                Timber.e(error, "Token refresh error!")
                throw error
            } finally {
                // The AuthState has already been updated when this callback happens. Just assign it.
                appAuthTokenStorage.state = authState
                dispose()
            }.also {
                Timber.d("Received access token: $it")
            }
        }
    }
}

/**
 * Use this function to always refresh an access-token, even if the current one is still valid.
 *
 * @return A a fresh new access-token.
 */
private suspend fun AuthorizationService.refreshTokenAlways(
    state: AuthState
): String = suspendCancellableCoroutine { cont ->

    val tokenRequest = state.createTokenRefreshRequest()
    performTokenRequest(tokenRequest) { resp, error ->
        state.update(resp, error)

        Timber.d("Token callback from refreshTokenAlways()")

        when {
            error != null -> cont.resumeWithException(error)
            resp?.accessToken != null -> cont.resume(resp.accessToken!!)
            else -> cont.resumeWithException(Error("Should not happen"))
        }
    }
}

/**
 * Use this function to only refresh an access-token when needed (default strategy).
 *
 * @return The current or a new access-token.
 */
private suspend fun AuthorizationService.refreshTokenIfNecessary(
    state: AuthState
): String = suspendCancellableCoroutine { cont ->

    state.performActionWithFreshTokens(this) { token, _, error ->
        Timber.d("Token callback from refreshTokenIfNecessary()")

        when {
            error != null -> cont.resumeWithException(error)
            token != null -> cont.resume(token)
            else -> cont.resumeWithException(Error("Should not happen"))
        }
    }
}
