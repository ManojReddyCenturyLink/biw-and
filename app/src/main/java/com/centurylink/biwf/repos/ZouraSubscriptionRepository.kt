package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.subscription.SubscriptionDates
import com.centurylink.biwf.service.network.ZuoraSubscriptionApiService
import com.centurylink.biwf.utility.preferences.Preferences
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZouraSubscriptionRepository @Inject constructor(
    private val preferences: Preferences,
    private val zuoraSubscriptionApiService: ZuoraSubscriptionApiService
) {

    private fun getAccountId(): String? {
        return preferences.getValueByID(com.centurylink.biwf.utility.preferences.Preferences.ACCOUNT_ID)
    }

    suspend fun getSubscriptionDate(): Either<String, Date> {
        val query =
            "SELECT Id, Name, Zuora__SubscriptionStartDate__c, Zuora__SubscriptionEndDate__c, Zuora__NextRenewalDate__c, Zuora__NextChargeDate__c FROM Zuora__Subscription__c WHERE Zuora__Account__c='%s'"
        val finalQuery = String.format(query, getAccountId()!!)
        val result: FiberServiceResult<SubscriptionDates> =
            zuoraSubscriptionApiService.getSubscriptionDate(finalQuery)
        return result.mapLeft { it.message?.message.toString() }
            .flatMap {
                val date = it.records.elementAtOrElse(0) { null }?.ZuoraSubscriptionEndDate
                date?.let {
                    Either.Right(it)
                } ?: Either.Left("Date is not available")
            }
    }
}
