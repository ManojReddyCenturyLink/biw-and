package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.service.network.AccountApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AccountServiceTest : BaseServiceTest() {

    private lateinit var accountApiService: AccountApiService

    @Before
    fun setup() {
        createServer()
        accountApiService = retrofit.create(AccountApiService::class.java)
    }

    @Test
    fun testGetAccountDetailsSuccess() = runBlocking {
        enqueueResponse("account.json")
        val posts: FiberServiceResult<AccountDetails> = accountApiService.getAccountDetails("12233")
        Assert.assertEquals(posts.map { it.name }, Either.Right("James Cameroon"))
        Assert.assertEquals(posts.map { it.Id }, Either.Right("001q000001GZ900AAD"))
    }

    @Test
    fun testGetAccountDetailsError() = runBlocking {
        val posts: FiberServiceResult<AccountDetails> = accountApiService.getAccountDetails("12233")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testSubmitServiceCallDetailsError() = runBlocking {
        val updatedServiceCallsAndTexts = UpdatedServiceCallsAndTexts(true)
        val posts: FiberServiceResult<Unit> =
            accountApiService.submitServiceCallDetails("12233", updatedServiceCallsAndTexts)
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}