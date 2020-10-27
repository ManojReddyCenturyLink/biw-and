package com.centurylink.biwf.utility.preferences

interface KeyValueStore {

    fun put(key: String, value: String): Boolean

    fun put(key: String, value: Int): Boolean

    fun get(key: String): String?

    fun getInt(key: String): Int?

    fun remove(key: String): Boolean

    fun getBoolean(key: String): Boolean?

    fun putBoolean(key: String, value: Boolean): Boolean
}
