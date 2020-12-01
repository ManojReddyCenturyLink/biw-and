package com.centurylink.biwf.model.modem

import com.google.gson.annotations.SerializedName

/**
 * Model class for modem id info
 */
data class ModemIdResponse(
    @SerializedName("totalSize") val totalSize: Int,
    @SerializedName("done") val done: Boolean,
    @SerializedName("records") val records: List<Records>
)

data class Records(
    @SerializedName("attributes") val attributes: com.centurylink.biwf.model.modem.Attributes,
    @SerializedName("Modem_Number__c") val modemNumberC: String
)

data class Attributes(
    @SerializedName("type") val type: String,
    @SerializedName("url") val url: String
)
