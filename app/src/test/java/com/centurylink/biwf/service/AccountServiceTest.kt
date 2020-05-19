package com.centurylink.biwf.service

import android.util.Log
import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.screens.home.HomeViewModel
import com.centurylink.biwf.service.network.AccountApiService
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.assertSame

class AccountServiceTest  : BaseServiceTest() {

    private lateinit var accountApiService: AccountApiService

    @Before
    fun setup() {
       createServer()
       accountApiService = retrofit.create(AccountApiService::class.java)
    }

    @Test
    fun testGetAccountDetailsSuccess() = runBlocking {
        enqueueResponse("account.json")
        lateinit var accountDetails:AccountDetails
        val posts:FiberServiceResult<AccountDetails> = accountApiService.getAccountDetails("12233")
        posts.map{ id->
          accountDetails = id
        }
        Assert.assertThat(accountDetails, IsNull.notNullValue())
        Assert.assertThat(accountDetails.name, CoreMatchers.`is`("James Cameroon"))
    }

    @Test
    fun testGetAccountDetailsError() = runBlocking {
        lateinit var fiberError: FiberHttpError
        val posts:FiberServiceResult<AccountDetails> = accountApiService.getAccountDetails("12233")
        posts.mapLeft{ id->
            fiberError = id
        }
        Assert.assertThat(fiberError, IsNull.notNullValue())
    }

    @Test
    fun testSubmitServiceCallDetailsError() = runBlocking {
        val updatedServiceCallsAndTexts = UpdatedServiceCallsAndTexts(true)
        lateinit var fiberError: FiberHttpError
        val posts:FiberServiceResult<Unit> =
            accountApiService.submitServiceCallDetails("12233",updatedServiceCallsAndTexts)
        posts.mapLeft {
            fiberError = it
        }
        Assert.assertThat(fiberError, IsNull.notNullValue())
    }
}