package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for traffic update network response
 */
data class UpdateNetworkResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: Boolean,
    @SerializedName("createErrorRecord")
    val createErrorRecord: Boolean =false,
    @SerializedName("uniqueErrorCode")
    val errorCode: String = ""
) : Serializable

