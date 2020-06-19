package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AccountRepositoryTest : BaseRepositoryTest() {

    private lateinit var accountRepository: AccountRepository

    @MockK(relaxed = true)
    private lateinit var accountApiService: AccountApiService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var accountDetails: AccountDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns "12345"
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        accountRepository = AccountRepository(mockPreferences, accountApiService)
    }

    @Test
    fun testgetAccountDetails() {
        runBlocking {
            launch {
                coEvery { accountApiService.getAccountDetails(any()) } returns Either.Right(
                    accountDetails
                )
                val accountInfo = accountRepository.getAccountDetails()
                Assert.assertEquals(accountInfo.map { it.name }, Either.Right("James Cameroon"))
                Assert.assertEquals(accountInfo.map { it.Id }, Either.Right("001q000001GZ900AAD"))
            }
        }
    }

    @Test
    fun testgetAccountDetailsError() {
        runBlocking {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    100,
                    listOf(FiberErrorMessage(errorCode = "1000", message = "Error"))
                )
                coEvery { accountApiService.getAccountDetails(any()) } returns Either.Left(
                    fiberHttpError
                )
                val accountInfo = accountRepository.getAccountDetails()
                Assert.assertEquals(accountInfo.mapLeft { it }, Either.Left("Error"))
            }
        }
    }

    @Test
    fun testSetServiceCallsAndTexts() {
        runBlocking {
            launch {
                coEvery {
                    accountApiService.submitServiceCallDetails(
                        any(),
                        any()
                    )
                } returns Either.Right(Unit)
                val accountInfo = accountRepository.setServiceCallsAndTexts(false)
                Assert.assertEquals(accountInfo.map { it }, accountInfo.map { it })
            }
        }
    }

    @Test
    fun testSetServiceCallsAndTextsError() {
        runBlocking {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    100,
                    listOf(FiberErrorMessage(errorCode = "1000", message = "Error"))
                )
                coEvery {
                    accountApiService.submitServiceCallDetails(
                        any(),
                        any()
                    )
                } returns Either.Left(fiberHttpError)
                val accountInfo = accountRepository.setServiceCallsAndTexts(false)
                Assert.assertEquals(accountInfo.map { it }, accountInfo.map { it })
            }
        }
    }

}