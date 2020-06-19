package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.payment.PaymentDetails
import com.centurylink.biwf.service.network.ZuoraPaymentService
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

class ZuoraPaymentRepositoryTest : BaseRepositoryTest() {

    private lateinit var zuoraPaymentRepository: ZuoraPaymentRepository

    @MockK(relaxed = true)
    private lateinit var zuoraPaymentService: ZuoraPaymentService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var paymentList: PaymentList

    private lateinit var paymentDetails: PaymentDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns "12345"
        val jsonPaymentListString = readJson("zuorapayment.json")
        paymentList = fromJson(jsonPaymentListString)
        val jsonPaymentDetailsString = readJson("zuorastatement.json")
        paymentDetails = fromJson(jsonPaymentDetailsString)
        zuoraPaymentRepository = ZuoraPaymentRepository(mockPreferences, zuoraPaymentService)
    }

    @Test
    fun getInvoicesListFromRepository() {
        runBlockingTest {
            launch {
                coEvery { zuoraPaymentService.getZuoraPaymentDetails(any()) } returns Either.Right(
                    paymentList
                )
                val zuoraPaymentDetails = zuoraPaymentRepository.getInvoicesList()
                Assert.assertEquals(
                    zuoraPaymentDetails.map { it.records[0].zuoraInvoiceC },
                    Either.Right("a1if0000002aKSbAAM")
                )
                Assert.assertEquals(
                    zuoraPaymentDetails.map { it.records[0].id },
                    Either.Right("a1Qf0000000aRQjEAM")
                )
            }
        }
    }


    @Test
    fun getPaymentInformationFromRepository() {
        runBlockingTest {
            launch {
                coEvery { zuoraPaymentService.getPaymentDetails(any()) } returns Either.Right(
                    paymentDetails
                )
                val zuoraPaymentDetails = zuoraPaymentRepository.getPaymentInformation("12345")
                Assert.assertEquals(
                    zuoraPaymentDetails.map { it.planCostWithoutTax },
                    Either.Right("$65.00")
                )
                Assert.assertEquals(
                    zuoraPaymentDetails.map { it.salesTaxAmount },
                    Either.Right("0.0")
                )
            }
        }
    }

    @Test
    fun getInvoicesListFromRepositoryError() {
        runBlockingTest {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    100,
                    listOf(FiberErrorMessage(errorCode = "1000", message = "Error"))
                )
                coEvery { zuoraPaymentService.getZuoraPaymentDetails(any()) } returns Either.Left(
                    fiberHttpError
                )
                val zuoraPaymentDetails = zuoraPaymentRepository.getInvoicesList()
                Assert.assertEquals(zuoraPaymentDetails.mapLeft { it }, Either.Left("Error"))

            }
        }
    }

    @Test
    fun getPaymentInformationFromRepositoryError() {
        runBlockingTest {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    100,
                    listOf(FiberErrorMessage(errorCode = "1000", message = "Error"))
                )
                coEvery { zuoraPaymentService.getPaymentDetails(any()) } returns Either.Left(
                    fiberHttpError
                )
                val zuoraPaymentDetails = zuoraPaymentRepository.getPaymentInformation("12345")
                Assert.assertEquals(zuoraPaymentDetails.mapLeft { it }, Either.Left("Error"))
            }
        }
    }
}