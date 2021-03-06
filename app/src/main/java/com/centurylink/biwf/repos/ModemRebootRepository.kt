package com.centurylink.biwf.repos

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.assia.ModemRebootResponse
import com.centurylink.biwf.model.assia.RebootModemRequest
import com.centurylink.biwf.repos.assia.AssiaTokenManager
import com.centurylink.biwf.service.network.OAuthAssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ModemRebootRepository -  This class interacts with Asia Cloudcheck modem  Services. This Repository class
 * gets the data from the network . It handles all the Modem Reboot related information from the Asia CloudCheck
 * backend. The View models can consume the Modem related information and display in the Activity
 * or Fragments.
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property assiaService instance for interacting with  the AssiaServices.
 * @property assiaTokenManager instance for getting the Asiatokenmanger.
 * @constructor Create empty Modem reboot repository
 */
@Singleton
class ModemRebootRepository @Inject constructor(
    private val preferences: Preferences,
    private val assiaService: OAuthAssiaService,
    private val assiaTokenManager: AssiaTokenManager
) {

    /**
     * This suspend function is used to reboot the Modem.
     *
     * @return ModemRebootResponse incase the API is success else error message will be displayed.
     */
    suspend fun rebootModem(): AssiaServiceResult<ModemRebootResponse> {
        return assiaService.rebootModem(RebootModemRequest(preferences.getAssiaId()))
    }

    companion object {
        const val REBOOT_STARTED_SUCCESSFULLY = 1000
    }
}
