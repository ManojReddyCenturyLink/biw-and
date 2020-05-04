package com.centurylink.biwf.service.auth

/**
 * Configures the app's authorization services.
 */
data class AuthServiceConfig(
    val configurationUrl: String,
    val clientId: String,
    val scope: String,
    val authRedirectUrl: String,
    val responseType: String,
    val display: String = "touch",
    val extraParams: Map<String,String> = emptyMap(),
    val policySignIn: String = ""
)
