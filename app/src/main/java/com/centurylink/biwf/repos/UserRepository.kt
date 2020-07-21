package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.assia.ModemIdResponse
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

    private fun storeAssiaId(assiaId: String) {
        preferences.saveAssiaId(assiaId)
    }

    suspend fun getAndSaveAssiaId(): Either<String, ModemIdResponse> {
        val query =
            "SELECT Modem_Number__c FROM WorkOrder WHERE AccountId='%s' AND Job_Type__c='Fiber Install - For Installations'"
        val finalQuery = String.format(query, preferences.getValueByID(Preferences.ACCOUNT_ID))
        val result = userApiService.getModemInfo(finalQuery)
        result.fold(
            ifLeft = {},
            ifRight = {
                it.getModemId()?.let { id ->
                    storeAssiaId(id)
                }
            }
        )
        return result.mapLeft { it.message?.message.toString() }
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