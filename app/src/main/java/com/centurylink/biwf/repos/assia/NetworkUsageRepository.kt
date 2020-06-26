package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.R
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import com.centurylink.biwf.model.usagedetails.UsageDetails
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.service.network.AssiaTrafficUsageService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.preferences.Preferences
import org.threeten.bp.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class NetworkUsageRepository @Inject constructor(
    private val assiaService: AssiaService,
    private val assiaTrafficUsageService: AssiaTrafficUsageService,
    private val integrationRestServices: IntegrationRestServices,
    private val preferences: Preferences
) {
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
        val result = assiaTrafficUsageService.getUsageDetails(
            getHeaderMap(getAssiaToken().accessToken, staMac, startDate, endDate)
        )
        return formatTrafficUsageResponse(result)
    }

    suspend fun getAssiaToken(): AssiaToken {
        return assiaService.getAssiaTokenWithTokenObject()
    }

    private fun getHeaderMap(
        token: String,
        staMac: String,
        startDate: String,
        endDate: String
    ): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()
        headerMap["staMac"] = staMac
        headerMap["startDate"] = startDate
        headerMap["endDate"] = endDate
        return headerMap
    }

    private fun formatTrafficUsageResponse(
        trafficUsageResponse: TrafficUsageResponse
    ): UsageDetails {
        var totalDownloadTraffic = 0.0
        var totalUploadTraffic = 0.0
        val downloadTrafficUnit: String
        val uploadTrafficUnit: String

        trafficUsageResponse.data.list?.forEach {
            totalDownloadTraffic += it.downLinkTraffic
            totalUploadTraffic += it.upLinkTraffic
        }

        if (totalUploadTraffic <= 999) {
            totalUploadTraffic.roundToInt().toString()
            uploadTrafficUnit = preferences.getContext().getString(R.string.mb_upload)
        } else if (totalUploadTraffic > 999 && totalUploadTraffic <= 999000) {
            totalUploadTraffic /= 1000
            uploadTrafficUnit = preferences.getContext().getString(R.string.gb_upload)
        } else {
            totalUploadTraffic /= 1000000
            uploadTrafficUnit = preferences.getContext().getString(R.string.tb_upload)
        }
        if (totalDownloadTraffic <= 999) {
            downloadTrafficUnit = preferences.getContext().getString(R.string.mb_download)
        } else if (totalDownloadTraffic > 999 && totalDownloadTraffic <= 999000) {
            totalDownloadTraffic /= 1000
            downloadTrafficUnit = preferences.getContext().getString(R.string.gb_download)
        } else {
            totalDownloadTraffic /= 1000000
            downloadTrafficUnit = preferences.getContext().getString(R.string.tb_download)
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