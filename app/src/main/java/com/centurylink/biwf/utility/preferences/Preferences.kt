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

    fun getUserId(userId: String) : String?{
        return store.get(userId)
    }

    fun removeUserId(userId: String?){
        store.remove(USER_ID)
    }

    val USER_ID = "USER_ID"
}