package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.mcafee.MacDeviceList
import com.centurylink.biwf.model.mcafee.MappingRequest
import com.centurylink.biwf.service.network.McafeeApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for all API calls from mcafee server with suspend functions
 */
@Singleton
class McafeeRepository @Inject constructor(
    private val preferences: Preferences,
    private val mcaFeeService: McafeeApiService
) {
    suspend fun getDeviceInfo(deviceList: List<String>): Either<String, List<MacDeviceList>> {
        val result =
            mcaFeeService.getDevicesMapping(MappingRequest(preferences.getAssiaId(), deviceList))
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (!it.code.equals(0)) {
                    Either.Left("No Mapping Devices Found ")
                }
                if (it.macDeviceList.isNullOrEmpty()) {
                    Either.Left("No Mapping Devices Found")
                }
                Either.Right(it.macDeviceList)
            }
        }
    }
}