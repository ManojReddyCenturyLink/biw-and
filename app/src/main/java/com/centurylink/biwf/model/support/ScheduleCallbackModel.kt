package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName

data class ScheduleCallbackModel(@SerializedName("topicsList") var reasonList: List<TopicList> = emptyList()): java.io.Serializable

data class TopicList(@SerializedName("topic") var topic: String? = ""): java.io.Serializable