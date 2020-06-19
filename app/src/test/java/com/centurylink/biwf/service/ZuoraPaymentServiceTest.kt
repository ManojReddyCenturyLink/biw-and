package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.payment.PaymentDetails
import com.centurylink.biwf.service.network.ZuoraPaymentService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ZuoraPaymentServiceTest : BaseServiceTest() {

    private lateinit var zuoraService: ZuoraPaymentService

    @Before
    fun setup() {
        createServer()
        zuoraService = retrofit.create(ZuoraPaymentService::class.java)
    }

    @Test
    fun testGetZuoraPaymentDetailsSuccess() = runBlocking {
        enqueueResponse("zuorapayment.json")
        val posts: FiberServiceResult<PaymentList> = zuoraService.getZuoraPaymentDetails("12233")
        Assert.assertEquals(
            posts.map { it.records[0].zuoraInvoiceC },
            Either.Right("a1if0000002aKSbAAM")
        )
        Assert.assertEquals(posts.map { it.records[0].id }, Either.Right("a1Qf0000000aRQjEAM"))
    }

    @Test
    fun testGetZuoraPaymentDetailsFailure() = runBlocking {
        val posts: FiberServiceResult<PaymentList> = zuoraService.getZuoraPaymentDetails("12233")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testGetPaymentStatementDetailsSuccess() = runBlocking {
        enqueueResponse("zuorastatement.json")
        val posts: FiberServiceResult<PaymentDetails> = zuoraService.getPaymentDetails("12233")
        Assert.assertEquals(posts.map { it.planCostWithoutTax }, Either.Right("$65.00"))
        Assert.assertEquals(posts.map { it.salesTaxAmount }, Either.Right("0.0"))
    }

    @Test
    fun testGetPaymentStatementDetailsFailure() = runBlocking {
        val posts: FiberServiceResult<PaymentDetails> = zuoraService.getPaymentDetails("12233")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}