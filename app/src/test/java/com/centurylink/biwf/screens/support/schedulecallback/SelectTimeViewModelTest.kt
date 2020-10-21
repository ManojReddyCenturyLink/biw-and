package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.support.SupportServicesReq
import com.centurylink.biwf.model.support.SupportServicesResponse
import com.centurylink.biwf.repos.SupportRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.TestCoroutineRule
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SelectTimeViewModelTest : ViewModelBaseTest() {

    @MockK
    private lateinit var viewModel: SelectTimeViewModel

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @MockK
    private lateinit var modemRebootMonitorService: ModemRebootMonitorService

    @MockK
    private lateinit var preferences: Preferences

    @MockK
    private lateinit var supportRepository: SupportRepository

    private lateinit var supportServiceResult: SupportServicesResponse

    private lateinit var supportServiceInfo: SupportServicesReq

    private var nextDay: Boolean = false

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        supportServiceInfo = fromJson(readJson("supportservice-req.json"))
        supportServiceResult = fromJson(readJson("supportservice-response.json"))
        run { analyticsManagerInterface }
        coEvery { supportRepository.supportServiceInfo(supportServiceInfo) } returns Either.Right(supportServiceResult)
        viewModel = SelectTimeViewModel(
            modemRebootMonitorService = modemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface,
            preferences = preferences,
            supportRepository = supportRepository
        )
    }

    @Test
    fun testSupportServiceSuccess() {
        runBlockingTest {
            coEvery {
                supportRepository.supportServiceInfo(supportServiceInfo)
            } returns Either.Right(supportServiceResult)
            viewModel.supportService(
                "111-111-1111",
                "false",
                "I want to know more about Fiber Internet",
                "2020-10-29 01:00:00",
                "Additional details"
            )
            Assert.assertEquals(false, viewModel.scheduleCallbackFlow.first())
        }
    }

    @Test
    fun testSupportServiceFailure() {
        runBlockingTest {
            coEvery { supportRepository.supportServiceInfo(supportServiceInfo) } returns Either.Left("")
            viewModel = SelectTimeViewModel(
                modemRebootMonitorService = modemRebootMonitorService,
                analyticsManagerInterface = analyticsManagerInterface,
                preferences = preferences,
                supportRepository = supportRepository
            )
        }
    }

    @Test
    fun testSupportInfo() {
        runBlockingTest {
            viewModel.supportService(
                "111-111-1111",
                "false",
                "I want to know more about Fiber Internet",
                "2020-10-29 01:00:00",
                "Additional details"
            )
            coEvery { supportRepository.supportServiceInfo(supportServiceInfo) }
        }
    }

    @Test
    fun testOnDateChanged() {
        runBlockingTest {
            viewModel.onDateChange()
            val expectedDate = viewModel.changeCallbackDateEvent.value!!.peekContent()
            Assert.assertEquals(expectedDate, Unit)
        }
    }

    @Test
    fun testOnTimeChanged() {
        runBlockingTest {
            viewModel.onTimeChange()
            val expectedTime = viewModel.changeCallbackTimeEvent.value!!.peekContent()
            Assert.assertEquals(expectedTime, Unit)
        }
    }

    @Test
    fun testOnCallBackDateSelected() {
        runBlockingTest {
            val cal: Calendar = Calendar.getInstance()
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
            viewModel.onCallbackDateSelected(cal.time)
            val expectedDate = viewModel.callbackDateUpdateEvent.value!!.peekContent()
            Assert.assertEquals(expectedDate, cal.time)
        }
    }

    @Test
    fun testOnCallbackTimeSelected() {
        runBlockingTest {
            val cal: Calendar = Calendar.getInstance()
            cal.set(
                0,
                0,
                0,
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
            )
            val timeSelected = SimpleDateFormat("hh:mma").format(cal.time)
            viewModel.onCallbackTimeSelected(timeSelected)
            val expectedDate = viewModel.callbackTimeUpdateEvent.value!!.peekContent()
            Assert.assertEquals(expectedDate, timeSelected)
        }
    }

    @Test
    fun testGetDefaultDateSlot() {
        runBlockingTest {
            var localDate = LocalDate.now()
            if (nextDay) localDate = localDate.plusDays(1)
            val dateReturned = viewModel.getDefaultDateSlot()
            Assert.assertEquals(
                dateReturned, localDate.format(DateTimeFormatter.ofPattern("MM/dd/YY"))
            )
        }
    }

    @Test
    fun testGetDefaultTimeSlot() {
        runBlockingTest {
            val resultTimeSlot1 = viewModel.getDefaultTimeSlot(5, 3)
            Assert.assertEquals(resultTimeSlot1, "03:15AM")
            val resultTimeSlot2 = viewModel.getDefaultTimeSlot(20, 3)
            Assert.assertEquals(resultTimeSlot2, "03:30AM")
            val resultTimeSlot3 = viewModel.getDefaultTimeSlot(35, 3)
            Assert.assertEquals(resultTimeSlot3, "03:45AM")
            val resultTimeSlot4 = viewModel.getDefaultTimeSlot(50, 3)
            Assert.assertEquals(resultTimeSlot4, "04:00AM")
            val resultTimeSlot5 = viewModel.getDefaultTimeSlot(50, 11)
            Assert.assertEquals(resultTimeSlot5, "12:00PM")
            val resultTimeSlot6 = viewModel.getDefaultTimeSlot(50, 23)
            Assert.assertEquals(resultTimeSlot6, "12:00AM")
            val resultTimeSlot7 = viewModel.getDefaultTimeSlot(50, 12)
            Assert.assertEquals(resultTimeSlot7, "01:00PM")
        }
    }

    @Test
    fun testFormatDateAndTime() {
        runBlockingTest {
            val resultDateTime1 = viewModel.formatDateAndTime("06/21/20", "03:45AM")
            Assert.assertEquals(resultDateTime1, "2020-06-21 03:45:00")
            val resultDateTime2 = viewModel.formatDateAndTime("06/21/20", "12:45AM")
            Assert.assertEquals(resultDateTime2, "2020-06-21 00:45:00")
            val resultDateTime3 = viewModel.formatDateAndTime("06/21/20", "09:45PM")
            Assert.assertEquals(resultDateTime3, "2020-06-21 21:45:00")
        }
    }
}