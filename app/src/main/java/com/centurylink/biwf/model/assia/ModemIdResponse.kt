package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ModemIdResponse(
    @SerializedName("done")
    val isDone: Boolean,
    @SerializedName("records")
    val list: List<ModemIdObject>
) : Serializable {
    fun getModemId(): String {
        return list[0].modemId
    }
}

data class ModemIdObject(
    @SerializedName("Modem_Number__c")
    val modemId: String
) : Serializable