package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.service.network.WifiStatusService
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 * WifiStatusRepository - This class interacts with wifiStatusService. This Repository class
 * gets the data from the network . It handles all the Wifi Enable /Disable related information from
 * the Apigee interacting with backend  and the View models can consume the Wifi status information and display in the Activity
 * or Fragments.
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property wifiStatusService wifiStatusService Instance for interacting with the Apigee Enable/Disable.
 * @constructor creates Wifi status repository
 */
@Singleton
class WifiStatusRepository @Inject constructor(
    private val preferences: Preferences,
    private val wifiStatusService: WifiStatusService
) {

    /**
     * The Suspend function used for the purpose of enabling the Network.
     *
     *
     * @param interfaceType - the Network interface type
     * @return UpnateNetworkResponse instance on Success and error message in case of failure
     */
    suspend fun enableNetwork(interfaceType: NetWorkBand): Either<String, UpdateNetworkResponse> {
        val queryMap = mutableMapOf<String, String>()
        queryMap[EnvironmentPath.WIFI_DEVICE_ID] = preferences.getAssiaId()
        queryMap[EnvironmentPath.INTERFACE_VALUE] = interfaceType.name
        val result = wifiStatusService.enableNetwork(queryMap)
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "1000") {
                return Either.Left(it.message)
            }
            return Either.Right(it)
        }
    }

    /**
     * The Suspend function used for the purpose of disabling the Network.
     *
     *
     * @param interfaceType - the Network interface type
     * @return UpdateNetworkResponse instance on Success and error message in case of failure
     */
    suspend fun disableNetwork(interfaceType: NetWorkBand): Either<String, UpdateNetworkResponse> {
        val queryMap = mutableMapOf<String, String>()
        queryMap[EnvironmentPath.WIFI_DEVICE_ID] = preferences.getAssiaId()
        queryMap[EnvironmentPath.INTERFACE_VALUE] = interfaceType.name
        val result = wifiStatusService.disableNetwork(queryMap)
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "1000") {
                return Either.Left(it.message)
            }
            return Either.Right(it)
        }
    }
}
