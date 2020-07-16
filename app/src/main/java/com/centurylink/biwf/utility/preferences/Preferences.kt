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

    fun getAccountId(): String {
        return store.get(ACCOUNT_ID) ?: ""
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

    fun getUserType(): Boolean? {
        return store.getBoolean(EXISTING_USER)
    }

    fun saveUserType(value: Boolean) {
        store.putBoolean(EXISTING_USER, value)
    }

    fun getHasSeenDialog(): Boolean {
        return store.getBoolean(HAS_SEEN_PROMPT)!!
    }

    fun saveHasSeenDialog() {
        store.putBoolean(HAS_SEEN_PROMPT, true)
    }

    fun saveAssiaId(assiaId: String) {
        store.put(ASSIA_ID, assiaId)
    }

    fun getAssiaId(): String {
        var asiaID = store.get(ASSIA_ID)
        if (asiaID.isNullOrEmpty()) {
            asiaID = "C4000XG1950000871"
        }
        return asiaID
    }

    fun saveSpeedTestFlag(boolean: Boolean) {
        store.putBoolean(SPEED_TEST_IS_RUNNING, boolean)
    }

    fun getSpeedTestFlag(): Boolean {
        return store.getBoolean(SPEED_TEST_IS_RUNNING) ?: false
    }

    fun saveSpeedTestUpload(uploadSpeed: String) {
        store.put(SPEED_TEST_UPLOAD_SPEED, uploadSpeed)
    }

    fun getSpeedTestUpload(): String? {
        return store.get(SPEED_TEST_UPLOAD_SPEED)
    }

    fun saveSpeedTestDownload(downloadSpeed: String) {
        store.put(SPEED_TEST_DOWNLOAD_SPEED, downloadSpeed)
    }

    fun getSpeedTestDownload(): String? {
        return store.get(SPEED_TEST_DOWNLOAD_SPEED)
    }

    fun saveLastSpeedTestTime(lastRanTime: String) {
        store.put(SPEED_TEST_LAST_TIME, lastRanTime)
    }

    fun getLastSpeedTestTime(): String? {
        return store.get(SPEED_TEST_LAST_TIME)
    }

    fun getSupportSpeedTest(): Boolean {
        return store.getBoolean(SUPPORT_SPEED_TEST_STARTED) ?: false
    }

    fun saveSupportSpeedTest(boolean: Boolean) {
        store.putBoolean(SUPPORT_SPEED_TEST_STARTED, boolean)
    }

    fun saveSpeedTestId(speedTestId: Int) {
        store.put(SPEED_TEST_ID, speedTestId)
    }

    fun getSpeedTestId(): Int? {
        return store.getInt(SPEED_TEST_ID)
    }


    companion object {
        const val USER_ID = "USER_ID"
        const val ACCOUNT_ID = "ACCOUNT_ID"
        const val CONTACT_ID = "CONTACT_ID"
        const val PLAN_NAME = "PLAN_NAME"
        const val BIOMETRIC = "BIOMETRICS"
        const val HAS_SEEN_PROMPT = "HAS_SEEN_PROMPT"
        const val EXISTING_USER = "EXISTING_USER"
        const val ASSIA_ID = "ASSIA_ID"
        const val SPEED_TEST_IS_RUNNING = "SPEED_TEST_IS_RUNNING"
        const val SPEED_TEST_UPLOAD_SPEED = "UPLOAD_SPEED"
        const val SPEED_TEST_DOWNLOAD_SPEED = "DOWNLOAD_SPEED"
        const val SPEED_TEST_LAST_TIME = "LAST_SPEED_TEST"
        const val SUPPORT_SPEED_TEST_STARTED = "SUPPORT_SPEED_TEST_STARTED"
        const val SPEED_TEST_ID = "SPEED_TEST_ID"
    }
}