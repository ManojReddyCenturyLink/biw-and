package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.subscription.SubscriptionDates
import com.centurylink.biwf.service.network.ZuoraSubscriptionApiService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ZouraSubscriptionRepositoryTest : BaseRepositoryTest() {

    private lateinit var zuoraSubscriptionRepository: ZouraSubscriptionRepository

    @MockK(relaxed = true)
    private lateinit var zuoraSubscriptionService: ZuoraSubscriptionApiService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var subscriptionDate: SubscriptionDates

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        val jsonString = readJson("subscriptiondate.json")
        subscriptionDate = fromJson(jsonString)
        zuoraSubscriptionRepository =
            ZouraSubscriptionRepository(mockPreferences, zuoraSubscriptionService)
    }

    @Test
    fun testgetSubscriptionDateSuccess() {
        runBlockingTest {
            launch {
                coEvery { zuoraSubscriptionService.getSubscriptionDate(any()) } returns Either.Right(
                    subscriptionDate
                )
                val subscriptionDetails = zuoraSubscriptionRepository.getSubscriptionDate()
                assert(true)
            }
        }
    }

    @Test
    fun testgetSubscriptionDateGemericError() {
        runBlockingTest {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(FiberErrorMessage(errorCode = Constants.ERROR_CODE_1000, message = Constants.ERROR))
                )
                coEvery { zuoraSubscriptionService.getSubscriptionDate(any()) } returns Either.Left(
                    fiberHttpError
                )
                val subscriptionDetails = zuoraSubscriptionRepository.getSubscriptionDate()
                Assert.assertEquals(subscriptionDetails.mapLeft { it }, Either.Left(Constants.ERROR))
            }
        }
    }

    @Test
    fun testgetSubscriptionDateError() {
        runBlockingTest {
            launch {
                val subs: SubscriptionDates =
                    SubscriptionDates(totalSize = 0, records = emptyList(), done = false)
                coEvery { zuoraSubscriptionService.getSubscriptionDate(any()) } returns Either.Right(
                    subs
                )
                val subscriptionDetails = zuoraSubscriptionRepository.getSubscriptionDate()
                Assert.assertEquals(
                    subscriptionDetails.mapLeft { it },
                    Either.Left("Date is not available")
                )
            }
        }
    }
}
