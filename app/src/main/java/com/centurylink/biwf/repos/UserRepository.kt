package com.centurylink.biwf.repos

import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val preferences: Preferences,
    private val userApiService: AccountApiService
) {

    fun storeUserId(accountId: String) {
        preferences.saveUserId(accountId)
    }

    fun getUserId(): String? {
        return preferences.getValueByID(Preferences.USER_ID)
    }
}