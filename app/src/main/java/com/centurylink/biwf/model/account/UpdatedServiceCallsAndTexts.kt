package com.centurylink.biwf.model.account

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdatedServiceCallsAndTexts(
    @SerializedName("Cell_Phone_Opt_In__c")
    val callsOptinC: Boolean
) : Serializable