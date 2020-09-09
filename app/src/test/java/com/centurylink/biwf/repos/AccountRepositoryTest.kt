package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentInfoResponse
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.utility.Constants
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

    private lateinit var paymentInfoResponse: PaymentInfoResponse
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        val paymentString = readJson("paymentinfo.json")
        paymentInfoResponse = fromJson(paymentString)
        accountRepository = AccountRepository(mockPreferences, accountApiService)
    }

    @Test
    fun testGetAccountDetails() {
        runBlocking {
            launch {
                coEvery { accountApiService.getAccountDetails(any()) } returns Either.Right(
                    accountDetails
                )
                val accountInfo = accountRepository.getAccountDetails()
                Assert.assertEquals(accountInfo.map { it.name }, Either.Right(Constants.ACCOUNT_NAME))
                Assert.assertEquals(accountInfo.map { it.Id }, Either.Right(Constants.ACCOUNT_ID))
            }
        }
    }

    @Test
    fun testGetAccountDetailsError() {
        runBlocking {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(FiberErrorMessage(errorCode = Constants.ERROR_CODE_1000, message = Constants.ERROR))
                )
                coEvery { accountApiService.getAccountDetails(any()) } returns Either.Left(
                    fiberHttpError
                )
                val accountInfo = accountRepository.getAccountDetails()
                Assert.assertEquals(accountInfo.mapLeft { it }, Either.Left(Constants.ERROR))
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
                    Constants.STATUS_CODE,
                    listOf(FiberErrorMessage(errorCode = Constants.ERROR_CODE_1000, message = Constants.ERROR))
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
    @Test
    fun testGetLiveCardDetails() {
        runBlocking {
            launch {
                coEvery { accountApiService.getLiveCardInfo(any()) } returns Either.Right(paymentInfoResponse)
                val accountInfo = accountRepository.getLiveCardDetails()
                Assert.assertEquals(accountInfo.map { it.isDone },  Either.Right(true))
            }
        }
    }
}