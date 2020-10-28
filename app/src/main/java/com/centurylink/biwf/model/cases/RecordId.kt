package com.centurylink.biwf.model.cases

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Model class for records info
 */
data class RecordId(
    @SerializedName("totalSize")
    @Expose
    val totalSize: Int? = null,

    @SerializedName("done")
    @Expose
    val done: Boolean? = null,

    @SerializedName("records")
    @Expose
    val records: List<RecordIdData> = emptyList()
)

data class RecordIdData(

    @SerializedName("Id")
    @Expose
    val Id: String? = null
)
