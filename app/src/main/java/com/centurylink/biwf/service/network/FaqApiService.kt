package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.Query

interface FaqApiService {

    @GET(EnvironmentPath.SALES_FORCE_QUERY)
    suspend fun getFaqDetails(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<Faq>

    @GET(EnvironmentPath.SALES_FORCE_QUERY)
    suspend fun getRecordTypeId(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<RecordId>
}
