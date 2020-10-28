package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.cases.CaseResponse
import com.centurylink.biwf.model.cases.Cases
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.cases.RecordIdData
import com.centurylink.biwf.service.network.CaseApiService
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
import java.util.*

class CaseRepositoryTest : BaseRepositoryTest() {

    private lateinit var caseRepository: CaseRepository

    @MockK(relaxed = true)
    private lateinit var caseApiService: CaseApiService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var case: Cases

    private lateinit var recordID: RecordId

    private lateinit var caseResponse: CaseResponse

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        val jsonString = readJson("case.json")
        val caseRespString = readJson("cancelsubscription.json")
        val recordIdString = readJson("caseid.json")
        caseResponse = fromJson(caseRespString)
        case = fromJson(jsonString)
        recordID = fromJson(recordIdString)
        caseRepository = CaseRepository(mockPreferences, caseApiService)
    }

    @Test
    fun testGetRecordTypeId() {
        runBlocking {
            launch {
                coEvery { caseApiService.getRecordTpeId(any()) } returns Either.Right(recordID)
                val recordIdDetails = caseRepository.getRecordTypeId()
                println(recordIdDetails)
                Assert.assertEquals(recordIdDetails.map { it }, Either.Right("a1Qf0000000aRQjEAM"))
            }
        }
    }

    @Test
    fun testGetEmptyRecordTypeId() {
        runBlocking {
            launch {
                coEvery { caseApiService.getRecordTpeId(any()) } returns Either.Right(RecordId())
                val recordIdDetails = caseRepository.getRecordTypeId()
                Assert.assertEquals(
                    recordIdDetails.mapLeft { it },
                    Either.Left("Record Id  Records is Empty")
                )
            }
        }
    }

    @Test
    fun testGetEmptyRecordTypeIdError() {
        runBlocking {
            launch {
                listOf(RecordIdData())
                coEvery { caseApiService.getRecordTpeId(any()) } returns Either.Right(
                    RecordId(
                        totalSize = 0,
                        records = listOf(RecordIdData())
                    )
                )
                val recordIdDetails = caseRepository.getRecordTypeId()
                Assert.assertEquals(
                    recordIdDetails.mapLeft { it },
                    Either.Left("Record Id  Records is Empty")
                )
            }
        }
    }

    @Test
    fun testGetEmptyRecordError() {
        runBlocking {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(
                        FiberErrorMessage(
                            errorCode = Constants.ERROR_CODE_1000,
                            message = "Record Id  Records is Empty"
                        )
                    )
                )
                coEvery { caseApiService.getRecordTpeId(any()) } returns Either.Left(fiberHttpError)
                val recordIdDetails = caseRepository.getRecordTypeId()
                Assert.assertEquals(
                    recordIdDetails.mapLeft { it },
                    Either.Left("Record Id  Records is Empty")
                )
            }
        }
    }

    @Test
    fun testCreateDeactivationRequest() {
        runBlocking {
            launch {
                coEvery { caseApiService.submitCaseForSubscription(any()) } returns Either.Right(
                    caseResponse
                )
                val submitInfo =
                    caseRepository.createDeactivationRequest(Date(), "", "", 4.0f, "", "")
                Assert.assertEquals(submitInfo.map { it.Id }, Either.Right("500f0000009AHOiAAO"))
                Assert.assertEquals(submitInfo.map { it.success }, Either.Right(true))
            }
        }
    }

    @Test
    fun testCreateDeactivationRequestWithNull() {
        runBlocking {
            launch {
                val emptyString: String? = null
                coEvery { caseApiService.submitCaseForSubscription(any()) } returns Either.Right(
                    caseResponse
                )
                val submitInfo = caseRepository.createDeactivationRequest(
                    Date(),
                    emptyString,
                    emptyString,
                    4.0f,
                    emptyString,
                    ""
                )
                Assert.assertEquals(submitInfo.map { it.Id }, Either.Right("500f0000009AHOiAAO"))
                Assert.assertEquals(submitInfo.map { it.success }, Either.Right(true))
            }
        }
    }

    @Test
    fun testCreateDeactivationRequestWithEmptyContactId() {
        runBlocking {
            launch {
                val emptyString: String? = null
                every { mockPreferences.getValueByID(any()) } returns emptyString
                coEvery { caseApiService.submitCaseForSubscription(any()) } returns Either.Right(
                    caseResponse
                )
                val submitInfo = caseRepository.createDeactivationRequest(
                    Date(),
                    emptyString,
                    emptyString,
                    4.0f,
                    emptyString,
                    ""
                )
                Assert.assertEquals(submitInfo.map { it.Id }, Either.Right("500f0000009AHOiAAO"))
                Assert.assertEquals(submitInfo.map { it.success }, Either.Right(true))
            }
        }
    }

    @Test
    fun testCreateDeactivationRequestWithEmptyfloat() {
        runBlocking {
            launch {
                val emptyString: String? = null
                val emptyfloat: Float? = null
                every { mockPreferences.getValueByID(any()) } returns emptyString
                coEvery { caseApiService.submitCaseForSubscription(any()) } returns Either.Right(
                    caseResponse
                )
                val submitInfo = caseRepository.createDeactivationRequest(
                    Date(),
                    emptyString,
                    emptyString,
                    emptyfloat,
                    emptyString,
                    ""
                )
                Assert.assertEquals(submitInfo.map { it.Id }, Either.Right("500f0000009AHOiAAO"))
                Assert.assertEquals(submitInfo.map { it.success }, Either.Right(true))
            }
        }
    }

    @Test
    fun testgetCaseIdSuccess() {
        runBlocking {
            launch {
                coEvery { caseApiService.getCaseNumber() } returns Either.Right(case)
                val casedetailsInfo = caseRepository.getCaseId()
                Assert.assertEquals(
                    casedetailsInfo.map { it.caseRecentItems[0].Id },
                    Either.Right("500f0000009ZtpSAAS")
                )
                Assert.assertEquals(
                    casedetailsInfo.map { it.caseRecentItems[0].caseNumber },
                    Either.Right("00011023")
                )
            }
        }
    }

    @Test
    fun testGetCaseIdError() {
        runBlocking {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(FiberErrorMessage(errorCode = Constants.ERROR_CODE_1000, message = "Error"))
                )
                coEvery { caseApiService.getCaseNumber() } returns Either.Left(fiberHttpError)
                val casedetailsInfo = caseRepository.getCaseId()
                Assert.assertEquals(casedetailsInfo.mapLeft { it }, Either.Left("Error"))
            }
        }
    }
}
