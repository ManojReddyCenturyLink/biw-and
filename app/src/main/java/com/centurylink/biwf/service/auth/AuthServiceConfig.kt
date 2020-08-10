package com.centurylink.biwf.service.auth

/**
 * Configures the app's authorization services.
 */
data class AuthServiceConfig(
    val authorizationEndpoint: String,
    val tokenEndpoint: String,
    val clientId: String,
    val authRedirectUrl: String,
    val scope: String,
    val responseType: String,
    val display: String = "touch",
    val revokeTokenEndpoint: String
)
