package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.usagedetails.NetworkListItem
import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import com.centurylink.biwf.model.usagedetails.UsageDetails
import com.centurylink.biwf.model.usagedetails.UsageDetailsHeader
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.service.network.AssiaTrafficUsageService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.DateUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssiaNetworkUsageRepository @Inject constructor(
    private val assiaService: AssiaService,
    private val assiaTrafficUsageService: AssiaTrafficUsageService,
    private val integrationRestServices: IntegrationRestServices
) {
    suspend fun getUsageDetails(dailyData: Boolean, staMac: String): TrafficUsageResponse {
        /*Calculate date parameters*/
        val startDate: String
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        val endDate = DateUtils.formatDateAssiaRequestFormat(calendar.timeInMillis)
        startDate = if (dailyData) {
            endDate
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -(14))
            DateUtils.formatDateAssiaRequestFormat(calendar.timeInMillis)
        }

        val usageDetailsBody = UsageDetailsHeader(staMac, startDate, endDate)
        val result = assiaTrafficUsageService.getUsageDetails(
            "traffic",
            getHeaderMap(getAssiaToken().accessToken, usageDetailsBody)
        )
        return formatTrafficUsageResponse(result, dailyData)
    }

    suspend fun getAssiaToken(): AssiaToken {
        return assiaService.getAssiaTokenWithTokenObject()
    }

    private fun getHeaderMap(
        token: String,
        usageDetailsHeader: UsageDetailsHeader
    ): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = "C4000XG1950000871"
        headerMap["staMac"] = usageDetailsHeader.staMac
        headerMap["startDate"] = usageDetailsHeader.startDate
        headerMap["endDate"] = usageDetailsHeader.endDate
        return headerMap
    }

    private fun formatTrafficUsageResponse(
        usageDetails: UsageDetails,
        dailyData: Boolean
    ): TrafficUsageResponse {
        val usageDetails = usageDetails.data.list
        var trafficUsageResponse = TrafficUsageResponse()
        usageDetails?.let { it ->
            if (it.isNotEmpty()) {
                var downLinkTraffic: Double = 0.0
                var upLinkTraffic: Double = 0.0
                it.forEach {
                    val usageList = it
                    val networkListItem = NetworkListItem(
                        upLinkTraffic = usageList.upLinkTraffic,
                        downLinkTraffic = usageList.downLinkTraffic,
                        upLinkPackets = usageList.upLinkPackets,
                        intf = usageList.intf,
                        stationMac = usageList.stationMac,
                        downLinkPackets = usageList.downLinkPackets,
                        downLinkPacketsFailed = usageList.downLinkPacketsFailed,
                        timestamp = usageList.timestamp,
                        trafficPattern = usageList.trafficPattern
                    )
                    if (dailyData) {
                        upLinkTraffic += networkListItem.upLinkTraffic
                        downLinkTraffic += networkListItem.downLinkTraffic
                    } else {
                        upLinkTraffic += networkListItem.upLinkTraffic / 1000
                        downLinkTraffic += networkListItem.downLinkTraffic / 1000
                    }
                }
                trafficUsageResponse = TrafficUsageResponse(downLinkTraffic, upLinkTraffic)
            }
        }
        return trafficUsageResponse
    }

    /*Mock Request if api is not working, for dev/testing purpose*/
    suspend fun getMockUsageDetails(dailyData: Boolean): Either<String, TrafficUsageResponse> {
        /*Calculate date Parameters*/
        val startDate: String
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        val endDate = DateUtils.formatDateAssiaRequestFormat(calendar.timeInMillis)

        if (dailyData) {
            startDate = endDate
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -(14))
            startDate = DateUtils.formatDateAssiaRequestFormat(calendar.timeInMillis)
        }
        val result: FiberServiceResult<UsageDetails> =
            integrationRestServices.getUsageDetails("traffic")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val usageDetails = it.data.list
            usageDetails?.let { it ->
                if (it.isEmpty()) {
                    Either.Left("Records are Empty")
                } else {
                    var downLinkTraffic: Double = 0.0
                    var upLinkTraffic: Double = 0.0
                    it.forEach {
                        val usageList = it
                        val networkListItem = NetworkListItem(
                            upLinkTraffic = usageList.upLinkTraffic,
                            downLinkTraffic = usageList.downLinkTraffic,
                            upLinkPackets = usageList.upLinkPackets,
                            intf = usageList.intf,
                            stationMac = usageList.stationMac,
                            downLinkPackets = usageList.downLinkPackets,
                            downLinkPacketsFailed = usageList.downLinkPacketsFailed,
                            timestamp = usageList.timestamp,
                            trafficPattern = usageList.trafficPattern
                        )
                        if (dailyData) {
                            upLinkTraffic += networkListItem.upLinkTraffic
                            downLinkTraffic += networkListItem.downLinkTraffic
                        } else {
                            upLinkTraffic += networkListItem.upLinkTraffic / 1000
                            downLinkTraffic += networkListItem.downLinkTraffic / 1000
                        }
                    }
                    Either.Right(TrafficUsageResponse(downLinkTraffic, upLinkTraffic))
                }
            } ?: Either.Left("Records are Empty")
        }
    }
}