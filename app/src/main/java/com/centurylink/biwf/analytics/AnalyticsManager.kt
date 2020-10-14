package com.centurylink.biwf.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics manager class used for different events used across the Application for Analytics.
 *
 * @property firebaseAnalytics Analytic instance
 * @constructor Create  Analytics manager
 */
@Singleton
class AnalyticsManager @Inject constructor(private val firebaseAnalytics: FirebaseAnalytics) {

    fun logScreenEvent(screenName: String) {
        logEvent(AnalyticsKeys.EVENT_TYPE_SCREEN_LAUNCH, screenName)
    }

    fun logButtonClickEvent(btnName: String) {
        logEvent(AnalyticsKeys.EVENT_TYPE_BUTTON_CLICKED, btnName)
    }

    fun logCardClickEvent(cardName: String) {
        logEvent(AnalyticsKeys.EVENT_TYPE_CARD_SELECTED, cardName)
    }

    fun logToggleChangeEvent(stateName: String, stateValue: Boolean) {
        logToggleEvent(AnalyticsKeys.EVENT_TYPE_TOGGLE_STATE_CHANGED, stateName, stateValue)
    }

    fun logListItemClickEvent(listItem: String) {
        logEvent(AnalyticsKeys.EVENT_TYPE_LIST_CLICKED, listItem)
    }

    fun logBioMetricsLogin(stateName: String, stateValue: Boolean) {
        logToggleEvent(AnalyticsKeys.EVENT_BIOMETRICS_LOGIN, stateName, stateValue)
    }

    fun logApiCall(apiName: String) {
        logEvent(AnalyticsKeys.EVENT_TYPE_API_CALL, apiName)
    }

    private fun logEvent(eventType: String, itemName: String) {
        val parameters = Bundle()
        parameters.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)

        synchronized(firebaseAnalytics) {
            firebaseAnalytics.logEvent(eventType, parameters)
        }
    }

    private fun logToggleEvent(eventType: String, itemName: String, itemValue: Boolean) {
        val parameters = Bundle()
        parameters.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        parameters.putBoolean(FirebaseAnalytics.Param.VALUE, itemValue)
        synchronized(firebaseAnalytics) {
            firebaseAnalytics.logEvent(eventType, parameters)
        }
    }
}