package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import com.centurylink.biwf.model.usagedetails.UsageDetails
import com.centurylink.biwf.screens.deviceusagedetails.NetworkTrafficUnits
import com.centurylink.biwf.service.network.AssiaTrafficUsageService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.preferences.Preferences
import org.threeten.bp.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * This class interacts with AssiaTrafficUsageService API . This Repository class
 * gets the data from the network . It handles all the Traffic usage related information from the CloudCheck API
 * backend  and the View models can consume the Traffic  related information and display in the Activity
 * or Fragments.
 *
 * @property assiaTokenManager - The Asia Token Manager instance
 * @property assiaTrafficUsageService - AssiaTraffic Usage Service API.
 * @property integrationRestServices - Local Integration Server instance.
 * @property preferences preference Instance for storing the value in shared preferences.
 * @constructor Create  Network usage repository
 */
@Singleton
class NetworkUsageRepository @Inject constructor(
    private val assiaTokenManager: AssiaTokenManager,
    private val assiaTrafficUsageService: AssiaTrafficUsageService,
    private val integrationRestServices: IntegrationRestServices,
    private val preferences: Preferences
) {
    /**
     * Get usage details of a Devices from the Backend.
     *
     * @param dailyData Boolean to get the daily data from the server.
     * @param staMac The Station mac Address of the
     * @return UsageDetails instance.
     */
    suspend fun getUsageDetails(dailyData: Boolean, staMac: String): UsageDetails {
        val startDate: String
        val endDate: String
        if (dailyData) {
            startDate = LocalDate.now().toString().plus("T00:00:00-0000")
            endDate = ""
        } else {
            startDate = LocalDate.now().minusDays(15).toString().plus("T00:00:00-0000")
            endDate = LocalDate.now().minusDays(1).toString().plus("T00:00:00-0000")
        }
        val headerMap = mutableMapOf<String, String>()
            headerMap["From"] = "mobile"
        val result = assiaTrafficUsageService.getUsageDetails(headerMap, preferences.getAssiaId(),startDate,staMac)
        return result.fold(
            ifRight = {
                formatTrafficUsageResponse(it)
            },
            ifLeft =   {
                throw IllegalStateException("Cannot read value")
            }
        )
    }

    private fun getHeaderMap(
        token: String,
        staMac: String,
        startDate: String,
        endDate: String
    ): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        // TODO remove "Authorization" from map when Cloudcheck URLs updated
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()
        headerMap["staMac"] = staMac
        headerMap["startDate"] = startDate
        headerMap["endDate"] = endDate
        return headerMap
    }

    /**
     * Format traffic usage response from the server to the format UI is in need of.
     *
     * @param trafficUsageResponse The Traffic Usage response from the Server
     * @return Usage Details format required by the API.
     */
    private fun formatTrafficUsageResponse(
        trafficUsageResponse: TrafficUsageResponse
    ): UsageDetails {
        var totalDownloadTraffic = 0.0
        var totalUploadTraffic = 0.0
        val downloadTrafficUnit: NetworkTrafficUnits
        val uploadTrafficUnit: NetworkTrafficUnits

        trafficUsageResponse.data.list?.forEach {
            totalDownloadTraffic += it.downLinkTraffic
            totalUploadTraffic += it.upLinkTraffic
        }

        if (totalUploadTraffic <= 999) {
            totalUploadTraffic.roundToInt().toString()
            uploadTrafficUnit = NetworkTrafficUnits.MB_UPLOAD
        } else if (totalUploadTraffic > 999 && totalUploadTraffic <= 999000) {
            totalUploadTraffic /= 1000
            uploadTrafficUnit = NetworkTrafficUnits.GB_UPLOAD
        } else {
            totalUploadTraffic /= 1000000
            uploadTrafficUnit = NetworkTrafficUnits.TB_UPLOAD
        }
        if (totalDownloadTraffic <= 999) {
            downloadTrafficUnit = NetworkTrafficUnits.MB_DOWNLOAD
        } else if (totalDownloadTraffic > 999 && totalDownloadTraffic <= 999000) {
            totalDownloadTraffic /= 1000
            downloadTrafficUnit = NetworkTrafficUnits.GB_DOWNLOAD
        } else {
            totalDownloadTraffic /= 1000000
            downloadTrafficUnit = NetworkTrafficUnits.TB_DOWNLOAD
        }

        return UsageDetails(
            totalDownloadTraffic,
            downloadTrafficUnit,
            totalUploadTraffic,
            uploadTrafficUnit
        )
    }

    /*Mock Request if api is not working, for dev/testing purpose*/
    suspend fun getMockUsageDetails(dailyData: Boolean): Either<String, UsageDetails> {
        val result: FiberServiceResult<TrafficUsageResponse> =
            integrationRestServices.getUsageDetails()

        return result.mapLeft { it.message?.message.toString() }.flatMap {
            Either.Right(formatTrafficUsageResponse(it))
        }
    }
}