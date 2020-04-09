package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FAQ(
    @SerializedName("videolist") var
    videolist: List<Videofaq> = emptyList(), @SerializedName("contentList") var
    questionlist: List<QuestionFAQ> = emptyList()
) : Serializable