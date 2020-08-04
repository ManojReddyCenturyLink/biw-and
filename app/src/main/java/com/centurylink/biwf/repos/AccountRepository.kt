package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentInfoResponse
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.preferences.Preferences
import timber.log.Timber
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

    private fun saveLineId(lineId: String) {
        preferences.saveLineId(lineId)
    }

    suspend fun getAccountDetails(): Either<String, AccountDetails> {
        val result: FiberServiceResult<AccountDetails> =
            accountApiService.getAccountDetails(getAccountId()!!)
        result.fold(
            ifLeft = { },
            ifRight = {
                val lineId = it.lineId
                Timber.i("lineId from server: $lineId")
                saveLineId(lineId ?: "")
            }
        )
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

    suspend fun getLiveCardDetails():Either<String,PaymentInfoResponse> {
        val query =
            "SELECT Credit_Card_Summary__c,Id,Name,Next_Renewal_Date__c,Zuora__BillCycleDay__c FROM Zuora__CustomerAccount__c WHERE Zuora__Account__c = '%s'"
        val finalQuery = String.format(query, preferences.getValueByID(Preferences.ACCOUNT_ID))
        val result = accountApiService.getLiveCardInfo(finalQuery)
       return result.mapLeft { it.message.toString() }
    }
}