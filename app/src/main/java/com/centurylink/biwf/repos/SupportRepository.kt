package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.support.SupportServicesReq
import com.centurylink.biwf.model.support.SupportServicesResponse
import com.centurylink.biwf.service.network.SupportService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Support repository SupportRepository class - This class interacts with Support Services API. This Repository class
 * gets the data from the network . It handles all the Schedule Callback related information from the Salesforce
 * backend  and the View models can consume the Callback related information and display the appropriate response in the Activity
 * or Fragments.
 *
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property supportService  supportApiService Instance for interacting with the Sales force Support Service API.
 * @constructor Creates  Account repository Instance.
 */

@Singleton
class SupportRepository @Inject constructor(
    private val preferences: Preferences,
    private val supportService: SupportService

) {

    /**
     * The Suspend function used for the making a callback appointment by the user
     *
     * @return Support Service Info if the API is success
     * Error in String format in case of API failure.
     */
    suspend fun supportServiceInfo(supportServicesReq: SupportServicesReq): Either<String, SupportServicesResponse> {
        val result: FiberServiceResult<SupportServicesResponse> =
            supportService.supportServiceInfo(
                supportServicesReq
            )
        return result.mapLeft { it.message?.message.toString() }
    }
}