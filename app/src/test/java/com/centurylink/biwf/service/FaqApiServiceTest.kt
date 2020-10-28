package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.service.network.FaqApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FaqApiServiceTest : BaseServiceTest() {

    private lateinit var faqApiService: FaqApiService

    @Before
    fun setup() {
        createServer()
        faqApiService = retrofit.create(FaqApiService::class.java)
    }

    @Test
    fun testGetFAQDetailsSuccess() = runBlocking {
        enqueueResponse("faqnosection.json")
        val posts: FiberServiceResult<Faq> = faqApiService.getFaqDetails("")
        Assert.assertEquals(posts.map { it.records[0].sectionC }, Either.Right("Manage my account"))
        Assert.assertEquals(posts.map { it.records[0].Id }, Either.Right("ka0f00000009Z9nAAE"))
    }

    @Test
    fun testGetFAQDetailsError() = runBlocking {
        val posts: FiberServiceResult<Faq> = faqApiService.getFaqDetails("12233")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}
