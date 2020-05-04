package com.centurylink.biwf.service.auth

import io.reactivex.rxjava3.core.Single

/**
 * Generates a brand new access-token by issuing a refresh-token-request if needed.
 */
interface AccessTokenGenerator {
    /**
     * @param tokenStorage Storage that must be updated with the new access-token.
     * @return a Single that emits a fresh new access-token.
     */
    fun generate(tokenStorage: TokenStorage<*>): Single<String>
}
