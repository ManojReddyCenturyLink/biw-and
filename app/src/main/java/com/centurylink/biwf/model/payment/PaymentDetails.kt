package com.centurylink.biwf.model.payment

import com.google.gson.annotations.SerializedName

class PaymentDetails(
    @SerializedName("Id")
    val invoiceId: String = "",
    @SerializedName("Account Email")
    val accountEmail: String = "",
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("CreatedDate")
    val createdDate: String = "",
    @SerializedName("CreatedById")
    val createdById: String = "",
    @SerializedName("LastModifiedDate")
    val lastModifiedDate: String = "",
    @SerializedName("SystemModstamp")
    val systemModStamp: String = "",

    @SerializedName("LastModifiedById")
    val lastModifiedById: String = "",

    @SerializedName("Product_Name__c")
    val productNameC: String = "",

    @SerializedName("Product_Plan_Name__c")
    val productPlanNameC: String = "",

    @SerializedName("Zuora__PaymentMethod__c")
    val zuoraPaymentMethod: String = "",

    @SerializedName("Zuora__PaymentMethodId__c")
    val zuoraPaymentMethodId: String = "",

    @SerializedName("ZAmountWithoutTax__c")
    val planCostWithoutTax: String = "",

    @SerializedName("Sales_Tax__c")
    val salesTaxAmount: String = "",

    @SerializedName("Tax_Amount__c")
    val taxAmount: String = "",

    @SerializedName("ZTaxAmount__c")
    val ZtaxAmount: String = "",

    @SerializedName("Amount_Without_Tax_Formula__c")
    val amountWithoutTaxFormula: String = "",

    @SerializedName("Tax_Amount_Formula__c")
    val taxAmountFormula: String = "",

    @SerializedName("Zuora__Invoice__c")
    val ZuoraInvoicec: String = "",

    @SerializedName("Zuora__Amount__c")
    val ZuoraAmountc: String = "",

    @SerializedName("Zuora__CreatedDate__c")
    val ZuoraCreatedDate: String = "",

    @SerializedName("Zuora__RefundedAmount__c")
    val zuoraRefundAmount: String = "",

    @SerializedName("Zuora__AppliedInvoiceAmount__c")
    val zuoraAppliedInvoiceAmount: String = ""

)