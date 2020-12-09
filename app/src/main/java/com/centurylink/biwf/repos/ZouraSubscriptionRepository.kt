package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.subscription.SubscriptionDates
import com.centurylink.biwf.model.subscriptionDetails.SubscriptionDetails
import com.centurylink.biwf.service.network.ZuoraSubscriptionApiService
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 *ZouraSubscriptionRepository - This class interacts with Zuora API Services. This Repository class
 * gets the data from the network . It handles all the Zuora Subscription related information from the Salesforce
 * backend  and the View models can consume the Subscription related information and display in the Activity
 * or Fragments.
 *
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property zuoraSubscriptionApiService Instance for interacting with the Sales force ZuoraSubscription API.s
 * @constructor Create  Zourasubscription repository
 */
@Singleton
class ZouraSubscriptionRepository @Inject constructor(
    private val preferences: Preferences,
    private val zuoraSubscriptionApiService: ZuoraSubscriptionApiService
) {
    /**
     * This method is used to get the Account Id that is stored in the  Shared Preferences
     * @return The Account Id.
     */
    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    /**
     * This method is used to get the Subscription date for the user
     *
     * @return Subscription Date in case of Success and Error Message in case of Error
     */
    suspend fun getSubscriptionDate(): Either<String, Date> {
        val finalQuery = String.format(EnvironmentPath.SUBSCRIPTION_DATES_QUERY, getAccountId()!!)
        val result: FiberServiceResult<SubscriptionDates> =
            zuoraSubscriptionApiService.getSubscriptionDate(finalQuery)
        return result.mapLeft { it.message?.message.toString() }
            .flatMap {
                val date = it.records.elementAtOrElse(0) { null }?.zuoraSubscriptionEndDate
                date?.let {
                    Either.Right(it)
                } ?: Either.Left("Date is not available")
            }
    }

    /**
     * This method is used to get the Subscription Details for the user
     *
     * @return Subscription Details in case of Success and Error Message in case of Error
     */
    suspend fun getSubscriptionDetails(): Either<String, SubscriptionDetails> {
        val finalQuery = String.format(EnvironmentPath.SUBSCRIPTION_DETAILS_QUERY, getAccountId()!!)
        val result: FiberServiceResult<SubscriptionDetails> =
            zuoraSubscriptionApiService.getSubscriptionDetails(finalQuery)
        return result.mapLeft { it.message?.message.toString() }
    }
}
