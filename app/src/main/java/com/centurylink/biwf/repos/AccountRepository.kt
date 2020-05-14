package com.centurylink.biwf.repos

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

    suspend fun getAccountDetails(): AccountDetails {
        return accountApiService.getAccountDetails(getAccountId()!!)
    }

    suspend fun setServiceCallsAndTexts(callValue: Boolean) {
        val updatedServiceCallsAndTexts = UpdatedServiceCallsAndTexts(callValue)
        val update = accountApiService.submitServiceCallDetails(
            getAccountId()!!,
            updatedServiceCallsAndTexts
        )
    }
}