package com.centurylink.biwf.service.auth

import io.reactivex.rxjava3.core.Single

/**
 * The service that provides to proper access-tokens to an HTTP API client.
 */
interface TokenService {
    /**
     * Storage used by this service to store and retrieve JWT tokens.
     */
    val tokenStorage: TokenStorage<*>

    /**
     * Generates a brand new access-token. If the access-token has expired, a request will go out
     * to obtain a fresh one using the refresh-token.
     *
     * Refrain from calling/using this property directly. Instead, use [accessToken].
     */
    val accessTokenGenerator: AccessTokenGenerator

    /**
     * Clears the stored JWT token.
     *
     * Any future call to the backend services will cause a HTTP Unauthorized failure (401)
     */
    fun clearToken()

    /**
     * Invalidates the access-token locally.
     *
     * Any future call to the backend services will cause a refresh-token request to be issued.
     */
    fun invalidateToken()
}

/**
 * Emits the freshest access-token and makes sure that if multiple requests for the access-token
 * happen at the same time, only one refresh-token-request is issued and the result is shared with all
 * subscribers.
 */
val TokenService.accessToken: Single<String>
    get() = Single.defer { accessTokenGenerator.generate(tokenStorage) }
        .toFlowable().share().singleOrError()

/**
 * Returns the value of an "Authorization" header. It is either empty, if authorization failed
 * for some reason or it returns the value "Bearer {access-token}" on success.
 *
 * Note that this could be a blocking call waiting for the OAuth token-endpoint to return a brand new
 * access-token.
 */
val TokenService.accessTokenHeader: String get() = try {
    "Bearer ${accessToken.blockingGet()}"
} catch (e: Exception) {
    ""
}
