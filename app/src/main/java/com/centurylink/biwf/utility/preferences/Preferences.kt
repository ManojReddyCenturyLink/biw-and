package com.centurylink.biwf.utility.preferences

import android.content.Context

class Preferences(private val store: KeyValueStore) {

    constructor(context: Context) : this(
        SharedPreferencesStore(
            context
        )
    )

    fun saveUserId(userId: String?) {
        store.put(USER_ID, userId!!)
    }

    fun savePlanName(planname: String?) {
        store.put(PLAN_NAME, planname!!)
    }

    fun getValueByID(userId: String): String? {
        return store.get(userId)
    }

    fun removeUserId() {
        store.remove(USER_ID)
    }

    fun saveAccountId(accountId: String?) {
        store.put(ACCOUNT_ID, accountId!!)
    }

    fun removeAccountId() {
        store.remove(ACCOUNT_ID)
    }

    fun saveContactId(accountId: String?) {
        store.put(CONTACT_ID, accountId!!)
    }

    fun removeContactId() {
        store.remove(CONTACT_ID)
    }

    fun getBioMetrics(): Boolean? {
        return store.getBoolean(BIOMETRIC)
    }

    fun saveBioMetrics(value: Boolean) {
        store.putBoolean(BIOMETRIC, value)
    }

    fun getHasSeenDialog(): Boolean {
        return store.getBoolean(HAS_SEEN_PROMPT)!!
    }

    fun saveHasSeenDialog() {
        store.putBoolean(HAS_SEEN_PROMPT, true)
    }

    companion object {
        const val USER_ID = "USER_ID"
        const val ACCOUNT_ID = "ACCOUNT_ID"
        const val CONTACT_ID = "CONTACT_ID"
        const val PLAN_NAME = "PLAN_NAME"
        const val BIOMETRIC = "BIOMETRICS"
        const val HAS_SEEN_PROMPT = "HAS_SEEN_PROMPT"
    }
}