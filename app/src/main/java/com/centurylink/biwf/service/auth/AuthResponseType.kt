package com.centurylink.biwf.service.auth

/**
 * Authorization responses returned when the auth-flow redirects back to the app.
 */
enum class AuthResponseType {
    /**
     * User has been authorized
     */
    AUTHORIZED,
    /**
     * User has been logged out.
     */
    LOGGED_OUT,
    /**
     * Authorization cancelled by user
     */
    CANCELLED,
    /**
     * An authorization error took place.
     */
    ERROR,
    /**
     * Some unknown redirect-url successfully somehow managed to return a proper response.
     * Still, the response is unexpected and unknown.
     */
    UNKNOWN_RESPONSE
}
