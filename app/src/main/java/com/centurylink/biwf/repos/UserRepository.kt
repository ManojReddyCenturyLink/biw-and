package com.centurylink.biwf.repos

import android.util.Log
import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.service.network.UserService
import com.centurylink.biwf.service.resp.ErrorResponse
import com.centurylink.biwf.service.resp.Failure
import com.centurylink.biwf.service.resp.Success
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val preferences: Preferences,
    private val userApiService: UserService
) {

    private fun storeUserId(accountId: String) {
        preferences.saveUserId(accountId)
    }

    private fun getUserId(): String? {
        return preferences.getValueByID(Preferences.USER_ID)
    }

    private fun storeAccountId(accountId: String) {
        preferences.saveAccountId(accountId)
    }

    private fun storeContactId(contactId: String) {
        preferences.saveContactId(contactId)
    }

    suspend fun getUserDetails(): UserDetails {
        val userId = getUserId()
        val result = userApiService.getCompleteUserDetails(userId!!)
        when(result){
            is Success->{
               Log.i("JAMMY","SUCCESS : "+result.value.name)
            }
            is Failure->{
                Log.i("JAMMY","Error : "+result.error)
            }
        }

    }

    suspend fun getUserInfo(): UserInfo {
        val userInfo = userApiService.qetUserInfo()
        storeUserId(userInfo.recentItems[0].Id!!)
        return userInfo
    }

    suspend fun resetPassWord(password: String){
        val userId = getUserId()
        val result  = userApiService.updatePassword(userId!!, UpdatedPassword(password))

    }
}