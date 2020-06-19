package com.centurylink.biwf

import com.centurylink.biwf.service.network.ApiServices
import javax.inject.Inject

open class BIWFAppTest : BIWFApp() {
    @Inject
    lateinit var apiServices: ApiServices

    fun setUp() {
    }
}