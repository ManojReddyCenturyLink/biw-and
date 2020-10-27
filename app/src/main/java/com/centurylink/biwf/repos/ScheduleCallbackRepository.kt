package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.support.ScheduleCallbackResponse
import com.centurylink.biwf.service.network.ScheduleCallbackService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedule Callback repository class - This class interacts with Picklist Services API. This Repository class
 * gets the data from the network . It handles all the Schedule Callback related information from the Salesforce
 * backend  and the View models can consume the Callback related information and display the appropriate response in the Activity
 * or Fragments.
 *
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property scheduleCallbackService schedule callback service Instance for interacting with the Sales force Schedule Callback Picklist API.
 * @constructor Creates  Schedule Callback repository Instance.
 */

@Singleton
class ScheduleCallbackRepository @Inject constructor(
    private val preferences: Preferences,
    private val scheduleCallbackService: ScheduleCallbackService

) {

    /**
     * The Suspend function used for the making getting picklist of customer care options
     *
     * @return Schedule Callback Response Info if the API is success
     * Error in String format in case of API failure.
     */
    suspend fun scheduleCallbackInfo(recordTypeId: String?): Either<String, ScheduleCallbackResponse> {
        val result: FiberServiceResult<ScheduleCallbackResponse> =
            scheduleCallbackService.scheduleCallbackPicklistInfo(recordTypeId)
        return result.mapLeft { it.message?.message.toString() }
    }
}
