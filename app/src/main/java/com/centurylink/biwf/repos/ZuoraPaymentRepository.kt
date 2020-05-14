package com.centurylink.biwf.repos

import com.centurylink.biwf.model.account.PaymentList
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

    suspend fun getInvoicesList(): PaymentList {
        val query: String =
            "SELECT+Id,Zuora__Invoice__c,CreatedDate+FROM+Zuora__Payment__c+WHERE+Zuora__Account__c+=+'%s'"
        val finalQuery = String.format(query, getAccountId()!!)
        return zuoraPaymentService.getZuoraPaymentDetails(finalQuery)
    }
}