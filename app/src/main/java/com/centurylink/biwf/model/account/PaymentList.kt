package com.centurylink.biwf.model.account

import com.google.gson.annotations.SerializedName

/**
 * Model class for payment details list
 */
data class PaymentList(
    @SerializedName("totalSize")
    val totalSize: Int = 0,
    @SerializedName("records")
    val records: List<RecordsItem>,
    @SerializedName("done")
    val done: Boolean = false
)