package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.faq.Faq
import retrofit2.http.GET
import retrofit2.http.Query

interface FaqApiService {

    @GET("query")
    suspend fun getFaqDetails(@Query("q") id: String): FiberServiceResult<Faq>

    @GET("query")
    suspend fun getRecordTypeId(@Query("q") id: String): FiberServiceResult<RecordId>
}