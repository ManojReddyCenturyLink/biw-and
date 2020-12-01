package com.centurylink.biwf.model.billing

import com.centurylink.biwf.model.account.BillingAddress
import com.google.gson.annotations.SerializedName

/**
 * Model class for billing info
 */
class BillingDetails(
    @SerializedName("Account Id")
    val accountId: String = "",
    @SerializedName("Account Email")
    val accountEmail: String = "",
    @SerializedName("Account Name")
    val accountName: String = "",
    @SerializedName("Account Zuora__BillToAddress1__c")
    val accountZuoraBillingAddress1: String = "",
    @SerializedName("Account Zuora__BillToAddress2__c")
    val accountZuoraBillingAddress2: String = "",

    @SerializedName("Account ProductPlanName__c")
    val accountProductPlanName: String = "",

    @SerializedName("Invoice Id")
    val invoiceId: String = "",

    @SerializedName("Invoice Zuora__AmountWithoutTax__c")
    val invoiceZuoraAccountWithoutTax: String,

    @SerializedName("Payment Id")
    val paymentId: String = "",

    @SerializedName("Zuora__PaymentMethod__c")
    val zuoraPaymentMethod: String = "",

    @SerializedName("ZAmountWithoutTax__c")
    val zAmountWithoutTax: String = "",

    @SerializedName("Zuora__Invoice__c")
    val zuoraInvoicec: String = "",

    @SerializedName("Zuora__Amount__c")
    val zuoraAmountc: String = "",

    @SerializedName("ZTaxAmount__c")
    val zTaxAmount: String = "",

    @SerializedName("Zuora__CreatedDate__c")
    val zuoraCreatedDate: String = "",

    @SerializedName("BillingAddress")
    val billingAddress: BillingAddress
)
