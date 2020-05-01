package com.centurylink.biwf.service.auth

import android.content.Intent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * Service for the launching the sign-in flow and handling the result.
 */
interface AuthService<S> {
    /**
     * Storage used by this service to store and retrieve JWT tokens.
     */
    val tokenStorage: TokenStorage<S>

    /**
     * Call this method to launch a sign-in flow.
     *
     * @return A Completable that either emits an error or completes successfully when the sign-in flow has launched.
     */
    fun launchSignInFlow(): Completable

    /**
     * Handles the sign-in/sign-up/logout redirect for the receiving Activity.
     * Provide its [Intent] to ensure a correct handling of the auth-flow response.
     *
     * @param androidIntent The [Intent] from the calling Activity that handles the redirect.
     * @return A Single that emits a [AuthResponseType]
     */
    fun handleResponse(androidIntent: Intent): Single<AuthResponseType>
}

/**
 * Creates instances of [AuthService]s for a given [AuthServiceHost].
 */
interface AuthServiceFactory<S> {
    fun create(host: AuthServiceHost): AuthService<S>
}
