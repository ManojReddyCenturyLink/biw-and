package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
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

    suspend fun getUserDetails(): Either<String, UserDetails> {
        val userId = getUserId()
        val result: FiberServiceResult<UserDetails> =
            userApiService.getCompleteUserDetails(userId!!)
        result.fold(
            ifLeft = { },
            ifRight = {
                storeAccountId(it.accountId!!)
                storeContactId(it.contactId!!)
            }
        )
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun getUserInfo(): Either<String, UserInfo> {
        val result: FiberServiceResult<UserInfo> = userApiService.qetUserInfo()
        result.fold(
            ifLeft = { },
            ifRight = {
                storeUserId(it.recentItems[0].Id!!)
            }
        )
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun resetPassWord(password: String): String {
        val userId = getUserId()
        val result: FiberServiceResult<Unit> =
            userApiService.updatePassword(userId!!, UpdatedPassword(password))
        val msg = result.fold(
            ifLeft = { it.message?.message.toString() },
            ifRight = { "" }
        )
        return msg
    }
}