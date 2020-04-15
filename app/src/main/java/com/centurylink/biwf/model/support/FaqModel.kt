package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName

data class FaqModel(@SerializedName("faqTopics") var faqTopics: List<FaqTopicsItem> = emptyList()): java.io.Serializable

data class VideoListItem(@SerializedName("duration") val duration: String = "",
                         @SerializedName("thumbnail") val thumbnail: String = "",
                         @SerializedName("videourl") val videoUrl: String = "",
                         @SerializedName("name") val name: String = "",
                         @SerializedName("description") val description: String = "",
                         @SerializedName("id") val id: Int = 0): java.io.Serializable

data class ContentListItem(@SerializedName("question") var question: String? = "",
                           @SerializedName("description") val description: String = "",
                           @SerializedName("id") val id: Int = 0): java.io.Serializable

data class FaqTopicsItem(@SerializedName("contentList") var contentList: List<ContentListItem>?,
                         @SerializedName("videolist") var videoList: List<VideoListItem>?,
                         @SerializedName("type") var type: String? = ""): java.io.Serializable
