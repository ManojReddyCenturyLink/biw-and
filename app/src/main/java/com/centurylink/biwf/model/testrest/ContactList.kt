package com.centurylink.biwf.model.testrest

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

data class ContactList(
    @SerializedName("totalSize")
    @Expose
    val totalSize: Int? = null,

    @SerializedName("done")
    @Expose
    val done: Boolean? = null,

    @SerializedName("records")
    @Expose
    val records: List<Record> = emptyList()
)

data class Record(
    @SerializedName("attributes")
    @Expose
    val attributes: RecordAttributes? = null,

    @SerializedName("Name")
    @Expose
    val name: String? = null
)

data class RecordAttributes(
    @SerializedName("type")
    @Expose
    val type: String? = null,

    @SerializedName("url")
    @Expose
    val url: String? = null
)

