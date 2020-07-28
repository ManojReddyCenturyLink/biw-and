package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentInfoResponse
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

    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    suspend fun getAccountDetails(): Either<String, AccountDetails> {
        val result: FiberServiceResult<AccountDetails> =
            accountApiService.getAccountDetails(getAccountId()!!)
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun setServiceCallsAndTexts(callValue: Boolean): String {
        val updatedServiceCallsAndTexts = UpdatedServiceCallsAndTexts(callValue)
        val result: FiberServiceResult<Unit> = accountApiService.submitServiceCallDetails(
            getAccountId()!!,
            updatedServiceCallsAndTexts
        )
        return result.fold(
            ifLeft = { it.message?.message.toString() },
            ifRight = { "" }
        )
    }

    suspend fun getLiveCardDetails(finalQuery: String):Either<String,PaymentInfoResponse> {
        val result = accountApiService.getLiveCardInfo(finalQuery)
       return result.mapLeft { it.message.toString() }
    }
}