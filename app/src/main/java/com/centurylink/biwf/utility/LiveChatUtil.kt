package com.centurylink.biwf.utility

import com.centurylink.biwf.BuildConfig
import com.centurylink.biwf.utility.preferences.Preferences
import com.salesforce.android.chat.core.ChatConfiguration
import com.salesforce.android.chat.ui.ChatUIConfiguration

class LiveChatUtil {
    companion object {
        val AMBASSADOR_BUTTON_FOR_STATE = "Colorado"

        fun getLiveChatUIConfiguration(sharedPreferences: Preferences): ChatUIConfiguration {
            var buttonId = BuildConfig.BUTTON_ID
            if (sharedPreferences.getBillingState().equals(AMBASSADOR_BUTTON_FOR_STATE)) {
                buttonId = BuildConfig.AMBASSADOR_BUTTON_ID
            }
            val chatConfiguration =
                ChatConfiguration.Builder(
                    BuildConfig.ORG_ID, buttonId,
                    BuildConfig.DEPLOYMENT_ID, BuildConfig.AGENT_POD).build()
            val uiConfig = ChatUIConfiguration.Builder()
                .chatConfiguration(chatConfiguration)
                .defaultToMinimized(false)
                .build()
            return uiConfig
        }
    }
}
