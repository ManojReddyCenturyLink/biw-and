package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val preferences: Preferences,
    private val accountApiService: AccountApiService
) {

    fun login(email: String, password: String, rememberMeFlag: Boolean): Boolean {
        return true
    }

    private fun storePlanName(planName: String) {
        preferences.savePlanName(planName)
    }

    private fun getPlanName(): String? {
        return preferences.getValueByID(Preferences.PLAN_NAME)
    }

    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    suspend fun getAccountDetails(): Either<String, AccountDetails> {
        val result: FiberServiceResult<AccountDetails> =
            accountApiService.getAccountDetails(getAccountId()!!)
        result.fold(
            ifLeft = {},
            ifRight = {
                it
            }
        )
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun setServiceCallsAndTexts(callValue: Boolean) : String {
        val updatedServiceCallsAndTexts = UpdatedServiceCallsAndTexts(callValue)
        val result: FiberServiceResult<Unit> = accountApiService.submitServiceCallDetails(
            getAccountId()!!,
            updatedServiceCallsAndTexts
        )
        val msg = result.fold(
            ifLeft = { it.message?.message.toString() },
            ifRight = { "" }
        )
        return msg
    }
}