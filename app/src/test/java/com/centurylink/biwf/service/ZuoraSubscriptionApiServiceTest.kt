package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.subscription.SubscriptionDates
import com.centurylink.biwf.service.network.ZuoraSubscriptionApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ZuoraSubscriptionApiServiceTest : BaseServiceTest() {

    private lateinit var zuoraService: ZuoraSubscriptionApiService

    @Before
    fun setup() {
        createServer()
        zuoraService = retrofit.create(ZuoraSubscriptionApiService::class.java)
    }

    @Test
    fun testGetSubscriptionDateSuccess() = runBlocking {
        enqueueResponse("subscriptiondate.json")
        val posts: FiberServiceResult<SubscriptionDates> = zuoraService.getSubscriptionDate("")
        Assert.assertEquals(posts.map { it.records[0].id }, Either.Right("a1df0000001GmxMAAS"))
    }

    @Test
    fun testGetSubscriptionDateFailure() = runBlocking {
        val posts: FiberServiceResult<SubscriptionDates> = zuoraService.getSubscriptionDate("")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}