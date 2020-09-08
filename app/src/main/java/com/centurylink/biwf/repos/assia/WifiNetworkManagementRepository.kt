package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.service.network.WifiNetworkApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun enableNetwork(interfaceType: NetWorkBand): Either<String, UpdateNetworkResponse> {
        val result = wifiNetworkApiService.enableNetwork(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMap(token = assiaTokenManager.getAssiaToken())
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "1000") {
                Either.Left(it.message)
            }
            Either.Right(it)
        }
    }

    suspend fun disableNetwork(interfaceType: NetWorkBand): Either<String,UpdateNetworkResponse> {
        val result = wifiNetworkApiService.disableNetwork(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMap(token = assiaTokenManager.getAssiaToken())
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
                if (it.code != "1000") {
                    Either.Left(it.message)
                }
                Either.Right(it)
        }
    }

    suspend fun getNetworkPassword(interfaceType: NetWorkBand): Either<String,NetworkDetails> {
        val result =  wifiNetworkApiService.getNetworkPassword(
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

    suspend fun updateNetworkPassword(
        interfaceType: NetWorkBand,
        updateNWPassword: UpdateNWPassword
    ): Either<String, UpdateNetworkResponse> {
        val result =  wifiNetworkApiService.updateNetworkPassword(
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

    private fun getHeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        return headerMap
    }

    private fun getHeaderMapWithContent(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["Content-Type"] = "application/json"
        return headerMap
    }
}