package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ModemInfoResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val modemInfo: ModemInfo
) : Serializable

data class ModemInfo(
    @SerializedName("lineId")
    val lineId: String = "",
    @SerializedName("modelName")
    val modelName: String = "",
   // @SerializedName("alive")
    //val alive: Boolean = false,
    @SerializedName("apInfos")
    val apInfoList: List<ApInfo> = emptyList()

) : Serializable

data class ApInfo(
    @SerializedName("deviceId")
    val deviceId: String? = null,
    @SerializedName("lineId")
    val lineId: String? = null,
    @SerializedName("modelName")
    val modelName: String? = null,
    @SerializedName("isRootAp")
    val isRootAp: Boolean = false,
    @SerializedName("isAlive")
    var isAlive: Boolean = false,
    @SerializedName("ssidMap")
    val ssidMap: HashMap<String, String> = HashMap(),
    @SerializedName("bssidMap")
    val bssidMap: HashMap<String, String> = HashMap()
) : Serializable


