package com.centurylink.biwf.repos

import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.service.network.UserService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val preferences: Preferences,
    private val userApiService: UserService
) {

    fun storeUserId(accountId: String) {
        preferences.saveUserId(accountId)
    }

    fun getUserId(): String? {
        return preferences.getValueByID(Preferences.USER_ID)
    }

    suspend fun getUserDetails(): UserDetails {
        val userId = getUserId()
        return userApiService.getCompleteUserDetails(userId!!)
    }

    suspend fun getUserInfo(): UserInfo {
        return userApiService.qetUserInfo()
    }

    suspend fun resetPassWord(password: String) {
        val userId = getUserId()
        userApiService.updatePassword(userId!!, UpdatedPassword(password))
    }
}