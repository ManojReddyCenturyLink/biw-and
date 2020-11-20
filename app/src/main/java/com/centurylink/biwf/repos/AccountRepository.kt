package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentInfoResponse
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Account repository AccountRepository class - This class interacts with Account API Services. This Repository class
 * gets the data from the network . It handles all the Account related information from the Salesforce
 * backend  and the View models can consume the Account related information and display in the Activity
 * or Fragments.
 *
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property accountApiService accountApiService Instance for interacting with the Sales force Accounts API.
 * @constructor Creates  Account repository Instance.
 */
@Singleton
class AccountRepository @Inject constructor(
    private val preferences: Preferences,
    private val accountApiService: AccountApiService
) {

    /**
     * This method is used to get the Account Id that is stored in the  Shared Preferences
     * @return The Account Id.
     */
    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    /**
     * This method stores the Line Id in the Preferences. The Line Id is used for the purpose of
     * getting the Devices information from Cloudcheck.
     * @param lineId The LineId used for getting the devices information from cloudcheck.
     */
    private fun saveLineId(lineId: String) {
        preferences.saveLineId(lineId)
    }

    /**
     * This method stores the Billint State in the Preferences. Billing State is used
     * to route the chat to sales force AMBASSADOR chat agent
     * @param state
     */
    private fun saveBillingState(state: String) {
        preferences.saveBillingState(state)
    }

    /**
     * The Suspend function used for the purpose of fetching the AccountDetails from the Salesforce
     * backend
     *
     * @return AccountDetails if the API is success it returns the AccountDetails instance
     * @return Error in String format in case of API failure.
     */
    suspend fun getAccountDetails(): Either<String, AccountDetails> {
        val result: FiberServiceResult<AccountDetails> =
            accountApiService.getAccountDetails(getAccountId()!!)
        result.fold(
            ifLeft = { },
            ifRight = {
                val lineId = it.lineId
                val billingState = it.billingAddress?.state
                Timber.i("lineId from server: $lineId")
                Timber.i("state from server: $billingState")
                saveLineId(lineId ?: "")
                saveBillingState(billingState ?: "")
            }
        )
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     * The Suspend function used for the purpose of setting the Services calls enablement for the user
     *
     * @param callValue the true/false value whether service call is enabled
     * @return Empty String if the API calls is Success and Error message in terms of errors
     */
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

    /**
     * The Suspend function used for the purpose of getting the liveCard Details
     *
     * @return PaymentInfoResponse gives the information if API is Success else it will give error
     * message in string.
     */
    suspend fun getLiveCardDetails(): Either<String, PaymentInfoResponse> {
        val finalQuery = String.format(
            EnvironmentPath.LIVE_CARD_DETAILS_QUERY,
            preferences.getValueByID(Preferences.ACCOUNT_ID)
        )
        val result = accountApiService.getLiveCardInfo(finalQuery)
        return result.mapLeft { it.message.toString() }
    }
}
