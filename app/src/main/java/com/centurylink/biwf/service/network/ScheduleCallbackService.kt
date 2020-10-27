package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.support.ScheduleCallbackResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Schedule Callback Picklist service Interface
 *
 */
interface ScheduleCallbackService {

    @GET(EnvironmentPath.API_SCHEDULE_CALLBACK_PATH)
    suspend fun scheduleCallbackPicklistInfo(
        @Path(EnvironmentPath.RECORD_TYPE_ID) recordTypeId: String?
    ): FiberServiceResult<ScheduleCallbackResponse>
}
