package com.centurylink.biwf.repos


import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.payment.PaymentDetails
import com.centurylink.biwf.service.network.ZuoraPaymentService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZuoraPaymentRepository @Inject constructor(
    private val preferences: Preferences,
    private val zuoraPaymentService: ZuoraPaymentService
) {

    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    suspend fun getInvoicesList(): Either<String, PaymentList> {
        val query: String =
            "SELECT Id, Zuora__Invoice__c, CreatedDate FROM Zuora__Payment__c WHERE Zuora__Account__c ='%s'"
        val finalQuery = String.format(query, getAccountId()!!)
        val result: FiberServiceResult<PaymentList> =
            zuoraPaymentService.getZuoraPaymentDetails(finalQuery)
        return result.mapLeft { it.message?.message.toString() }
    }

    suspend fun getPaymentInformation(invoiceId: String): Either<String, PaymentDetails> {
        val result: FiberServiceResult<PaymentDetails> =
            zuoraPaymentService.getPaymentDetails(invoiceId)
        return result.mapLeft { it.message?.message.toString() }
    }
}