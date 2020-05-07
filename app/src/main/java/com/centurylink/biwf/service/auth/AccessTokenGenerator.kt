package com.centurylink.biwf.service.auth

/**
 * Generates a brand new access-token by issuing a refresh-token-request if needed.
 */
interface AccessTokenGenerator {
    /**
     * @param tokenStorage Storage that must be updated with the new access-token.
     * @return A fresh new access-token.
     */
    suspend fun generate(tokenStorage: TokenStorage<*>): String
}
