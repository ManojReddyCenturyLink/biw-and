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

    fun getUserId(userId: String): String? {
        return store.get(userId)
    }

    fun removeUserId() {
        store.remove(USER_ID)
    }

    fun getValueByID(userId: String): String? {
        return store.get(userId)
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

    companion object {
        val USER_ID = "USER_ID"
        val ACCOUNT_ID = "ACCOUNT_ID"
        val CONTACT_ID = "CONTACT_ID"
    }
}