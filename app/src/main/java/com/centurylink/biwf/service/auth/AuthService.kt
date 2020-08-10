package com.centurylink.biwf.service.auth

import android.content.Intent

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
     * This method resumes when the sign-in flow has launched.
     */
    suspend fun launchSignInFlow()

    /**
     * Call this method to revoke the access token on behalf of the client.
     *
     * This method returns true if the access token has been successfully revoked.
     */
    suspend fun revokeToken(): Boolean

    /**
     * Handles the sign-in/sign-up/logout redirect for the receiving Activity.
     * Provide its [Intent] to ensure a correct handling of the auth-flow response.
     *
     * @param androidIntent The [Intent] from the calling Activity that handles the redirect.
     * @return An [AuthResponseType]
     */
    suspend fun handleResponse(androidIntent: Intent): AuthResponseType
}

/**
 * Creates instances of [AuthService]s for a given [AuthServiceHost].
 */
interface AuthServiceFactory<S> {
    fun create(host: AuthServiceHost): AuthService<S>
}
