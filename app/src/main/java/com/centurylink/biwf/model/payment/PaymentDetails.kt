package com.centurylink.biwf.model.payment

import com.google.gson.annotations.SerializedName

class PaymentDetails(
    @SerializedName("Id")
    val invoiceId: String? = null,
    @SerializedName("CreatedDate")
    val createdDate: String? = null,
    @SerializedName("CreatedById")
    val createdById: String? = null,
    @SerializedName("LastModifiedDate")
    val lastModifiedDate: String? = null,

    @SerializedName("SystemModstamp")
    val systemModStamp: String? = null,

    @SerializedName("LastModifiedById")
    val lastModifiedById: String? = null,

    @SerializedName("Product_Name__c")
    val productNameC: String? = null,

    @SerializedName("Product_Plan_Name__c")
    val productPlanNameC: String? = null,

    @SerializedName("Zuora__PaymentMethod__c")
    val zuoraPaymentMethod: String? = null,

    @SerializedName("Zuora__PaymentMethodId__c")
    val zuoraPaymentMethodId: String? = null,

    @SerializedName("ZAmountWithoutTax__c")
    val planCostWithoutTax: String? = null,

    @SerializedName("Sales_Tax__c")
    val salesTaxAmount: String? = null,

    @SerializedName("Tax_Amount__c")
    val taxAmount: String? = null,

    @SerializedName("ZTaxAmount__c")
    val ZtaxAmount: String? = null,

    @SerializedName("Amount_Without_Tax_Formula__c")
    val amountWithoutTaxFormula: String? = null,

    @SerializedName("Tax_Amount_Formula__c")
    val taxAmountFormula: String? = null,

    @SerializedName("Zuora__Invoice__c")
    val ZuoraInvoicec: String? = null,

    @SerializedName("Zuora__Amount__c")
    val ZuoraAmountc: String? = null,

    @SerializedName("Zuora__CreatedDate__c")
    val ZuoraCreatedDate: String? = null,

    @SerializedName("Zuora__RefundedAmount__c")
    val zuoraRefundAmount: String? = null,

    @SerializedName("Zuora__AppliedInvoiceAmount__c")
    val zuoraAppliedInvoiceAmount: String? = null

)