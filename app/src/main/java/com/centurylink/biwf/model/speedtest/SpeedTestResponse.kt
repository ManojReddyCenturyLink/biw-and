package com.centurylink.biwf.model.speedtest

import com.google.gson.annotations.SerializedName

data class SpeedTestResponse(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: SpeedTestNestedResults
)

data class SpeedTestNestedResults(
    @SerializedName("list")
    val listOfData: Array<SpeedTestResult>,
    @SerializedName("name")
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpeedTestNestedResults

        if (!listOfData.contentEquals(other.listOfData)) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = listOfData.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

data class SpeedTestResult(
    @SerializedName("average")
    val speedAvg: Int,
    @SerializedName("timestamp")
    val timeStamp: String
)