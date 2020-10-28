package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import com.centurylink.biwf.model.cases.CaseResponse
import com.centurylink.biwf.model.cases.Cases
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.service.network.CaseApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CaseApiServiceTest : BaseServiceTest() {

    private lateinit var caseApiService: CaseApiService

    @Before
    fun setup() {
        createServer()
        caseApiService = retrofit.create(CaseApiService::class.java)
    }

    @Test
    fun testGetCaseNumberSuccess() = runBlocking {
        enqueueResponse("case.json")
        val posts: FiberServiceResult<Cases> = caseApiService.getCaseNumber()
        Assert.assertEquals(posts.map { it.caseRecentItems[0].Id }, Either.Right("500f0000009ZtpSAAS"))
        Assert.assertEquals(posts.map { it.caseRecentItems[0].caseNumber }, Either.Right("00011023"))
    }

    @Test
    fun GetCaseNumberFailure() = runBlocking {
        val posts: FiberServiceResult<Cases> = caseApiService.getCaseNumber()
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testPostsubmitCaseForSubscriptionSuccess() = runBlocking {
        enqueueResponse("cancelsubscription.json")
        val posts: FiberServiceResult<CaseResponse> = caseApiService.submitCaseForSubscription(CaseCreate())
        Assert.assertEquals(posts.map { it.Id }, Either.Right("500f0000009AHOiAAO"))
        Assert.assertEquals(posts.map { it.success }, Either.Right(true))
    }

    @Test
    fun testPostsubmitCaseForSubscriptionFailure() = runBlocking {
        val posts: FiberServiceResult<CaseResponse> = caseApiService.submitCaseForSubscription(CaseCreate())
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testGetRecordIdSuccess() = runBlocking {
        enqueueResponse("caseid.json")
        val posts: FiberServiceResult<RecordId> = caseApiService.getRecordTpeId("")
        Assert.assertEquals(posts.map { it.records[0].Id }, Either.Right("a1Qf0000000aRQjEAM"))
    }

    @Test
    fun testGetRecordIdFailure() = runBlocking {
        val posts: FiberServiceResult<RecordId> = caseApiService.getRecordTpeId("")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}
