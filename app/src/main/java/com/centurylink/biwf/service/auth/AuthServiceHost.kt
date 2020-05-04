package com.centurylink.biwf.service.auth

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent

/**
 * This interface needs to be implemented by Activities/Fragments that either
 * can launch the sign-in/sign-up/logout screen
 * or that can handle the sign-in/sign-up/logout responses.
 */
interface AuthServiceHost {
    /**
     * Activity/Service that hosts the [AuthService] (through the components's ViewModel)
     */
    val hostContext: Context

    /**
     * For screens that launch a sign-in/sign-up flow, this must represent the custom-tab configuration/intent
     */
    val customTabsIntent: CustomTabsIntent? get() = null

    /**
     * For screens that launch a sign-in/sign-up flow, this must return an intent for an Activity that can handle the authorization
     * response, i.e. the Activity that can handle the policy's redirect-URL.
     */
    fun getCompletionIntent(context: Context): Intent? = null
}
