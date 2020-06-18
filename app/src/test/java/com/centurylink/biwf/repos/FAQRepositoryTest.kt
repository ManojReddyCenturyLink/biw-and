package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.cases.RecordIdData
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.service.network.FaqApiService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FAQRepositoryTest : BaseRepositoryTest() {

    private lateinit var faqRepository: FAQRepository

    @MockK(relaxed = true)
    private lateinit var faqService: FaqApiService

    private lateinit var faq: Faq

    private lateinit var recordID: RecordId

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val jsonString = readJson("faqnosection.json")
        faq = fromJson(jsonString)
        faqRepository = FAQRepository(faqService)
        val recordIdString = readJson("caseid.json")
        recordID = fromJson(recordIdString)
    }

    @Test
    fun testFaqDetailsfromRepository() {
        runBlockingTest {
            launch {
                coEvery { faqService.getFaqDetails(any()) } returns Either.Right(faq)
                val faqDetails = faqRepository.getFAQQuestionDetails("12345")
                Assert.assertEquals(faqDetails.map { it.records[0].sectionC }, Either.Right("Manage my account"))
                Assert.assertEquals(faqDetails.map { it.records[0].Id }, Either.Right("ka0f00000009Z9nAAE"))
                Assert.assertEquals(faqDetails.map { it.records[0].articleNumber }, Either.Right("000001063"))
            }
        }
    }

    @Test
    fun testFaqDetailsfromRepositoryError() {
        runBlockingTest {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    100,
                    listOf(
                        FiberErrorMessage(
                            errorCode = "1000",
                            message = "RecordType Id is Empty"
                        )
                    )
                )
                coEvery { faqService.getFaqDetails(any()) } returns Either.Left(fiberHttpError)
                val faqDetailsempty = faqRepository.getFAQQuestionDetails("")
                Assert.assertEquals(
                    faqDetailsempty.mapLeft { it },
                    Either.Left("RecordType Id is Empty")
                )
            }
        }
    }

    @Test
    fun testGetRecordTypeId() {
        runBlocking {
            launch {
                coEvery { faqService.getRecordTypeId(any()) } returns Either.Right(recordID)
                val recordIdDetails = faqRepository.getKnowledgeRecordTypeId()
                println(recordIdDetails)
                Assert.assertEquals(recordIdDetails.map { it}, Either.Right("a1Qf0000000aRQjEAM"))
            }
        }
    }
    @Test
    fun testGetEmptyRecordTypeId() {
        runBlocking {
            launch {
                coEvery { faqService.getRecordTypeId(any()) } returns Either.Right(RecordId())
                val recordIdDetails = faqRepository.getKnowledgeRecordTypeId()
                Assert.assertEquals(recordIdDetails.mapLeft { it}, Either.Left("Record Id  Records is Empty"))
            }
        }
    }

    @Test
    fun testGetEmptyRecordTypeIdError() {
        runBlocking {
            launch {
                listOf(RecordIdData())
                coEvery { faqService.getRecordTypeId(any()) } returns Either.Right(
                    RecordId(totalSize = 0 , records = listOf(
                        RecordIdData()
                    ))
                )
                val recordIdDetails = faqRepository.getKnowledgeRecordTypeId()
                Assert.assertEquals(recordIdDetails.mapLeft { it}, Either.Left("Record Id  Records is Empty"))
            }
        }
    }

    @Test
    fun testGetEmptyRecordError() {
        runBlocking {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    100,
                    listOf(FiberErrorMessage(errorCode = "1000", message = "Record Id  Records is Empty"))
                )
                coEvery { faqService.getRecordTypeId(any()) } returns Either.Left(fiberHttpError)
                val recordIdDetails = faqRepository.getKnowledgeRecordTypeId()
                Assert.assertEquals(recordIdDetails.mapLeft { it}, Either.Left("Record Id  Records is Empty"))
            }
        }
    }
}