package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.billing.BillingDetails
import com.centurylink.biwf.service.network.BillingApiServices
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val preferences: Preferences,
    private val billingApiDetails: BillingApiServices
) {
    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    suspend fun getBillingDetails(): Either<String, List<BillingDetails>> {
        val result: FiberServiceResult<List<BillingDetails>> = billingApiDetails.getBillingDetails()
        return result.mapLeft { it.message?.message.toString() }
    }
}