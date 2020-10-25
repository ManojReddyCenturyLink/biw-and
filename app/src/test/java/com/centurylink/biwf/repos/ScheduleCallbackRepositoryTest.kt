package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.support.ScheduleCallbackResponse
import com.centurylink.biwf.service.network.ScheduleCallbackService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.any
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ScheduleCallbackRepositoryTest: BaseRepositoryTest() {
    private lateinit var scheduleCallbackRepository: ScheduleCallbackRepository

    @MockK(relaxed = true)
    private lateinit var scheduleCallbackService: ScheduleCallbackService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var schedulecallbackinfo: ScheduleCallbackResponse

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        schedulecallbackinfo = fromJson(readJson("schedulecallback-info.json"))
        scheduleCallbackRepository = ScheduleCallbackRepository(mockPreferences, scheduleCallbackService)
    }

    @Test
    fun testScheduleCallbackPicklistInfo() {
        runBlockingTest {
            coEvery {
                scheduleCallbackService.scheduleCallbackPicklistInfo(any())
            } returns Either.Right(schedulecallbackinfo)
            val supportServicesResponse=scheduleCallbackRepository.scheduleCallbackInfo(any())
            Assert.assertEquals(supportServicesResponse.map { it.eTag }, Either.Right("021aa02d9c1907505e308474508dd843"))

        }
    }
}