package com.centurylink.biwf.utility.preferences

import android.content.Context
import com.centurylink.biwf.BuildConfig

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

    private fun removeAccountId() {
        store.remove(ACCOUNT_ID)
    }

    fun saveContactId(accountId: String?) {
        store.put(CONTACT_ID, accountId!!)
    }

    private fun removeContactId() {
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

    fun saveLineId(lineId: String) {
        store.put(LINE_ID, lineId)
    }

    fun getLineId(): String {
        var lineId = store.get(LINE_ID)
        // TODO This needs to be removed before launch
        if (lineId.isNullOrEmpty()) {
            if (BuildConfig.DEBUG) {
                lineId = BuildConfig.LINE_ID
            }
        }
        return lineId ?: ""
    }

    private fun removeLineId() {
        store.remove(LINE_ID)
    }

    /**
     * Save assia id - It will save assia id shared preferences
     *
     * @param assiaId
     */
    fun saveAssiaId(assiaId: String) {
        store.put(ASSIA_ID, assiaId)
    }

    /**
     * Get assia id -It will get assia id shared preferences
     *
     * @return -It will return assia id as string
     */
    fun getAssiaId(): String {
        var asiaID = store.get(ASSIA_ID)
        // TODO: Pre-launch, remove this or add an if (Build.DEBUG) condition
        if (asiaID.isNullOrEmpty()) {
            if (BuildConfig.DEBUG) {
                asiaID = BuildConfig.MODEM_ID
            }
        }
        return asiaID ?: ""
    }

    fun setInstallationStatus(status: Boolean) {
        store.putBoolean(INSTALLATION_STATUS, status)
    }

    fun getInstallationStatus(): Boolean {
        return store.getBoolean(INSTALLATION_STATUS) ?: false
    }

    private fun removeAssiaId() {
        store.remove(ASSIA_ID)
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

    fun saveSpeedTestId(speedTestId: String) {
        store.put(SPEED_TEST_ID, speedTestId)
    }

    fun getSpeedTestId(): String? {
        return store.get(SPEED_TEST_ID)
    }

    // Should only be used for logout, currently
    fun clearUserSettings() {
        saveBioMetrics(false)
        saveUserType(false)
        removeAccountId()
        removeContactId()
        removeLineId()
        removeAssiaId()
    }


    companion object {
        const val USER_ID = "USER_ID"
        const val ACCOUNT_ID = "ACCOUNT_ID"
        const val CONTACT_ID = "CONTACT_ID"
        const val PLAN_NAME = "PLAN_NAME"
        const val BIOMETRIC = "BIOMETRICS"
        const val HAS_SEEN_PROMPT = "HAS_SEEN_PROMPT"
        const val EXISTING_USER = "EXISTING_USER"
        const val LINE_ID = "LINE_ID"
        const val ASSIA_ID = "ASSIA_ID"
        const val SPEED_TEST_IS_RUNNING = "SPEED_TEST_IS_RUNNING"
        const val SPEED_TEST_UPLOAD_SPEED = "UPLOAD_SPEED"
        const val SPEED_TEST_DOWNLOAD_SPEED = "DOWNLOAD_SPEED"
        const val SPEED_TEST_LAST_TIME = "LAST_SPEED_TEST"
        const val SUPPORT_SPEED_TEST_STARTED = "SUPPORT_SPEED_TEST_STARTED"
        const val SPEED_TEST_ID = "SPEED_TEST_ID"
        const val INSTALLATION_STATUS = "INSTALLATION_STATUS"

    }
}