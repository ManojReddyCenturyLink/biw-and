package com.centurylink.biwf

import com.centurylink.biwf.network.api.ApiServices
import javax.inject.Inject

open class BIWFAppTest :BIWFApp(){
    @Inject
    lateinit var apiServices: ApiServices

    fun setUp() {
    }
}