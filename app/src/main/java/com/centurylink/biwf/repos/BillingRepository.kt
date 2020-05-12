package com.centurylink.biwf.repos

import android.util.Log
import com.centurylink.biwf.model.billing.BillingDetails
import com.centurylink.biwf.service.network.BillingApiServices
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val preferences: Preferences,
    private val billingApiDetails: BillingApiServices
) {


    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    suspend fun getBillingDetails(invoiceId:String): BillingDetails {
        val query:String = "SELECT+Id,Zuora__Invoice__c,CreatedDate+FROM+Zuora__Payment__c+WHERE+Zuora__Account__c+=+'%s'"
        val finalQuery = String.format(query,invoiceId)
        Log.i("JAMMY","Query :"+finalQuery)
        return billingApiDetails.getBillStatementDetails(finalQuery)
    }
}