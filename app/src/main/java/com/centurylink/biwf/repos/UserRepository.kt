package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.service.network.UserService
import com.centurylink.biwf.utility.preferences.Preferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class interacts with User API Services. This Repository class
 * gets the data from the network . It handles all the Contact related information from the Salesforce
 * backend  and the View models can consume the Contact related information and display in the Activity
 * or Fragments.
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property userApiService Instance for interacting with the Sales force UserService API.
 * @constructor Create  User repository
 */
@Singleton
class UserRepository @Inject constructor(
    private val preferences: Preferences,
    private val userApiService: UserService
) {

    /**
     * This method is used to Store the User Id in shared Preferences.
     *
     * @param accountId stores the User Id.
     */
    private fun storeUserId(accountId: String) {
        preferences.saveUserId(accountId)
    }

    /**
     * This method is used to Store the User Id in shared Preferences.
     *
     * @return  the UserId
     */
    private fun getUserId(): String? {
        return preferences.getValueByID(Preferences.USER_ID)
    }

    /**
     * This method is used to Store the Account Id in shared Preferences.
     *
     * @param accountId the Contact Id value.
     */
    private fun saveAccountId(accountId: String) {
        preferences.saveAccountId(accountId)
    }

    /**
     *This method is used to Store the Account Id in shared Preferences.
     *
     * @param contactId the Contact Id value.
     */
    private fun saveContactId(contactId: String) {
        preferences.saveContactId(contactId)
    }

    /**
     * The Suspend function used for the purpose of fetching the UserDetails from the Salesforce
     * backend.
     *
     * @return UserDetails The Userdetails instance if success / error in case of API response.
     */
    suspend fun getUserDetails(): Either<String, UserDetails> {
        val userId = getUserId()
        val result: FiberServiceResult<UserDetails> =
            userApiService.getCompleteUserDetails(userId!!)
        result.fold(
            ifLeft = { },
            ifRight = {
                val accountId = it.accountId
                val contactId = it.contactId
                Timber.i("accountId from server: $accountId")
                Timber.i("contactId from server: $contactId")
                saveAccountId(accountId ?: "")
                saveContactId(contactId ?: "")
            }
        )
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     *The Suspend function used for the purpose of fetching the UserInfo from the Salesforce
     * backend.
     *
     * @return UserInfo instance if success / error in case of API response.
     */
    suspend fun getUserInfo(): Either<String, UserInfo> {
        val result: FiberServiceResult<UserInfo> = userApiService.qetUserInfo()
        result.fold(
            ifLeft = { },
            ifRight = {
                if (it.recentItems.isNotEmpty())
                    storeUserId(it.recentItems[0].Id!!)
            }
        )
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
    The Suspend function used for the purpose of fetching the UserInfo from the Salesforce
     * backend.
     *
     * @param password
     * @return
     */
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