package com.centurylink.biwf.model.faq

import com.google.gson.annotations.SerializedName

/**
 * Model class for frequently asked questions info
 */
data class Faq(
    @SerializedName("totalSize")
    val totalSize: Int? = null,

    @SerializedName("done")
    val done: Boolean? = null,

    @SerializedName("records")
    val records: List<FaqRecord> = emptyList()
)

data class FaqRecord(

    @SerializedName("ArticleNumber")
    val articleNumber: String? = null,

    @SerializedName("ArticleTotalViewCount")
    val articleTotalViewCount: String? = null,

    @SerializedName("Article_Content__c")
    val articleContent: String? = null,

    @SerializedName("Article_Url__c")
    val articleUrl: String? = null,

    @SerializedName("Language")
    val language: String? = null,

    @SerializedName("Title")
    val title: String? = null,

    @SerializedName("Section__c")
    val sectionC: String? = null,

    @SerializedName("Id")
    val id: String? = null
)
