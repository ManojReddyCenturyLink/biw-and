package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QuestionFAQ(
    @SerializedName("id") var id: Int,
    @SerializedName("question") var name: String? = null,
    @SerializedName("description") var description: String? = null
) : Serializable