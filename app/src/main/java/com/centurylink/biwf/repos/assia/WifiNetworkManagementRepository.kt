package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.service.network.WifiNetworkApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wifi network management repository- This Repository class Performs the Network management operations.
 * This class talks with the Wifi NetworkAPI service.
 * gets the data from the network . It handles all the Traffic usage related information from the CloudCheck API
 * backend  and the View models can consume the Traffic  related information and display in the Activity
 * or Fragments.
 *
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property wifiNetworkApiService - WifiNetworkApiService class for Managing Password and Network Name.
 * @property assiaTokenManager
 * @constructor Create empty Wifi network management repository
 */
@Singleton
class WifiNetworkManagementRepository @Inject constructor(
    private val preferences: Preferences,
    private val wifiNetworkApiService: WifiNetworkApiService,
    private val assiaTokenManager: AssiaTokenManager
) {
    suspend fun getNetworkName(interfaceType: NetWorkBand): Either<String, NetworkDetails> {
        val result = wifiNetworkApiService.getNetworkName(
            preferences.getAssiaId(),
            interfaceType, getHeaderMap(token = assiaTokenManager.getAssiaToken())
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "1000") {
                Either.Left(it.message)
            }
            Either.Right(it)
        }
    }

    /**
     * The Suspend function used for updating the Network Name from the server.
     *
     * @param interfaceType The interface type can be of either Bands
     * @param updateNetworkName Updates the Network name configured by the User.
     * @return UpdateNetworkResponse on success and Error Message in case of Failure..
     */
    suspend fun updateNetworkName(
        interfaceType: NetWorkBand,
        updateNetworkName: UpdateNetworkName
    ): Either<String, UpdateNetworkResponse> {
        val result = wifiNetworkApiService.updateNetworkName(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMap(token = assiaTokenManager.getAssiaToken()),
            updateNetworkName
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "1000") {
                Either.Left(it.message)
            }
            Either.Right(it)
        }
    }

    /**
     * The Suspend function used for getting the Network Password from the server.
     *
     * @param interfaceType the Band types of the server.
     * @return NetworkDetails from the server.
     */
    suspend fun getNetworkPassword(interfaceType: NetWorkBand): Either<String, NetworkDetails> {
        val result = wifiNetworkApiService.getNetworkPassword(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMapWithContent(token = assiaTokenManager.getAssiaToken())
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
                if (it.code != "1000") {
                    Either.Left(it.message)
                }
                Either.Right(it)
        }
    }

    /**
     * The Suspend function used for Updating the Network Password to the server.
     *
     * @param interfaceType the Band types of the server.
     * @param updateNWPassword The Password that needs to be sent to the server.
     * @return UpdateNetworkResponse The Network response on Success and error message in case of failure.
     */
    suspend fun updateNetworkPassword(
        interfaceType: NetWorkBand,
        updateNWPassword: UpdateNWPassword
    ): Either<String, UpdateNetworkResponse> {
        val result = wifiNetworkApiService.updateNetworkPassword(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMapWithContent(token = assiaTokenManager.getAssiaToken()), updateNWPassword
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
                if (it.code != "1000") {
                    Either.Left(it.message)
                }
                Either.Right(it)
        }
    }

    /**
     * The header map function used for configuring the request headers.
     *
     * @param token the token instance.
     * @return Map with all Hearer information for the request.
     */
    private fun getHeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        return headerMap
    }

    /**
     * The header map function used for configuring the header map with contents .
     *
     * @param token The token instance
     * @return Map with all header information for the request.
     */
    private fun getHeaderMapWithContent(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["Content-Type"] = "application/json"
        return headerMap
    }
}
