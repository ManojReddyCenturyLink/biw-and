package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.usagedetails.DailyUsageDetails
import com.centurylink.biwf.model.usagedetails.NetworkListItem
import com.centurylink.biwf.model.usagedetails.UsageDetails
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.preferences.Preferences
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUsageRepository @Inject constructor(
    private val preferences: Preferences,
    private val integrationRestServices: IntegrationRestServices
) {

    suspend fun getUsageDetails(dailyData: Boolean): Either<String, DailyUsageDetails> {
        val oauth = "bearer 51d22afa-e14d-46d2-8a12-8a303dc12c1c"
        val assiaId = "C4000XG1950000871"
        val staMac = "8C:85:90:AC:B7:15"
        val apiPath = "traffic"
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
            integrationRestServices.getUsageDetails("apiPath")
        //TODO: call api here
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
                    Either.Right(DailyUsageDetails(downLinkTraffic, upLinkTraffic))
                }
            } ?: Either.Left("Records are Empty")
        }
    }
}