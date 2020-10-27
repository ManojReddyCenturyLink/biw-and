package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.payment.PaymentDetails
import com.centurylink.biwf.service.network.ZuoraPaymentService
import com.centurylink.biwf.utility.EnvironmentPath
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ZuoraPaymentRepository -  This class interacts with Zuora Payment API Services. This Repository class
 * gets the data from the network . It handles all the Zuora Payment related information from the Salesforce
 * backend  and the View models can consume the Zuora Payment related information and display in the Activity
 * or Fragments.
 *
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property zuoraPaymentService Instance for interacting with the Sales force Zuora Payment APIs.
 * @constructor Create empty Zuora payment repository
 */
@Singleton
class ZuoraPaymentRepository @Inject constructor(
    private val preferences: Preferences,
    private val zuoraPaymentService: ZuoraPaymentService
) {

    /**
     * This method is used to get the Account Id that is stored in the  Shared Preferences
     * @return The Account Id.
     */
    private fun getAccountId(): String? {
        return preferences.getValueByID(Preferences.ACCOUNT_ID)
    }

    /**
     * This suspend method is used to get the Invoice List for the user
     *
     * @return PaymentList instance incase the API is successful / error message on failure.
     */
    suspend fun getInvoicesList(): Either<String, PaymentList> {
        val finalQuery = String.format(EnvironmentPath.INVOICE_LIST_QUERY, getAccountId()!!)
        val result: FiberServiceResult<PaymentList> =
            zuoraPaymentService.getZuoraPaymentDetails(finalQuery)
        return result.mapLeft { it.message?.message.toString() }
    }

    /**
     * This suspend method is used to get the Payment information for the users.
     *
     * @param invoiceId The Invoice for which the payment information is needed.
     * @return PaymentDetails instance if the API is successful and error message in case of failure.
     */
    suspend fun getPaymentInformation(invoiceId: String): Either<String, PaymentDetails> {
        val result: FiberServiceResult<PaymentDetails> =
            zuoraPaymentService.getPaymentDetails(invoiceId)
        return result.mapLeft { it.message?.message.toString() }
    }
}
