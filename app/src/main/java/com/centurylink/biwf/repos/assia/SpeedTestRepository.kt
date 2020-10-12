package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.speedtest.SpeedTestRequestResult
import com.centurylink.biwf.model.speedtest.SpeedTestStatus
import com.centurylink.biwf.model.speedtest.SpeedTestStatusRequest
import com.centurylink.biwf.model.speedtest.SpeedTestStatusResponse
import com.centurylink.biwf.service.network.SpeedTestService
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SpeedTest Repository
 *
 * @property preferences
 * @property speedTestService
 */
@Singleton
class SpeedTestRepository @Inject constructor(
    private val preferences: Preferences,
    private val speedTestService: SpeedTestService
) {

    suspend fun startSpeedTest():  Either<String,SpeedTestRequestResult> {
        val result =
            speedTestService.getSpeedTestDetails(
                SpeedTestStatusRequest(
                    EnvironmentPath.CALL_BACK_URL,
                    requestId = "",
                    assiaId = preferences.getAssiaId()
                )
            )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.requestId.isNullOrEmpty()) {
                return Either.Left("Request not found")
            }
            val speedTestRequestResult = SpeedTestRequestResult(1000,"", it.requestId,it.success)
            return Either.Right(speedTestRequestResult)
        }
    }

    suspend fun checkSpeedTestStatus(speedTestId: String): Either<String, SpeedTestStatus> {
        val result =
            speedTestService.getSpeedTestStatusDetails(
                SpeedTestStatusRequest(
                    EnvironmentPath.CALL_BACK_URL,
                    requestId = speedTestId,
                    assiaId = preferences.getAssiaId()
                )
            )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.status == "ERROR") {
                return Either.Left("Request not found")
            }
            if(it.statusResponse.code != 1000){
                return Either.Left("Request not found")
            }
            return Either.Right(it.statusResponse)
        }
    }

    suspend fun getSpeedTestResults(speedTestId: String): Either<String, SpeedTestStatusResponse> {
        val result =
            speedTestService.getSpeedTestStatusDetails(
                SpeedTestStatusRequest(
                    EnvironmentPath.CALL_BACK_URL,
                    requestId = speedTestId,
                    assiaId = preferences.getAssiaId()
                )
            )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.status == "ERROR") {
                return Either.Left("Request not found")
            }
            if(it.statusResponse.code != 1000){
                return Either.Left("Request not found")
            }
            return Either.Right(it)
        }
    }
}