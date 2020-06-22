package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.usagedetails.NetworkListItem
import com.centurylink.biwf.model.usagedetails.UsageDetails
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUsageRepository @Inject constructor(
    private val preferences: Preferences,
    private val integrationRestServices: IntegrationRestServices
) {

    suspend fun getDailyUsageDetails(): Either<String, Double> {

        /*Header Parameters*/
        val oauth = "bearer 51d22afa-e14d-46d2-8a12-8a303dc12c1c"
        val startDate = "2020-06-01T22:00:00-0300"
        val endDate = "2020-06-08T22:00:00-0300"
        val assiaId = "C4000XG1950000871"
        val staMac = "8C:85:90:AC:B7:15"
        val apiPath = "traffic"

        val result: FiberServiceResult<UsageDetails> =
            integrationRestServices.getUsageDetails("apiPath")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val usageDetails = it.data.list
            usageDetails?.let { it ->
                if (it.isEmpty()) {
                    Either.Left("Records are Empty")
                } else {
                    var upLinkTraffic: Double
                    val usageList = it[0]
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
                    upLinkTraffic = networkListItem.upLinkTraffic
                    Either.Right(upLinkTraffic)
                }
            } ?: Either.Left("Records are Empty")
        }
    }

    suspend fun getMonthlyUsageDetails(): Either<String, Double> {

        /*Header Parameters*/
        val oauth = "bearer 51d22afa-e14d-46d2-8a12-8a303dc12c1c"
        val startDate = "2020-06-01T22:00:00-0300"
        val endDate = "2020-06-08T22:00:00-0300"
        val assiaId = "C4000XG1950000871"
        val staMac = "8C:85:90:AC:B7:15"
        val apiPath = "traffic"

        val result: FiberServiceResult<UsageDetails> =
            integrationRestServices.getUsageDetails("apiPath")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            val usageDetails = it.data.list
            usageDetails?.let { it ->
                if (it.isEmpty()) {
                    Either.Left("Records are Empty")
                } else {
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
                        upLinkTraffic += networkListItem.upLinkTraffic / 1000
                    }
                    Either.Right(upLinkTraffic)
                }
            } ?: Either.Left("Records are Empty")
        }
    }
}