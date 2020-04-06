package com.centurylink.biwf

import com.centurylink.biwf.di.component.DaggerApplicationComponent
import com.centurylink.biwf.network.api.ApiServices
import junit.framework.Test
import javax.inject.Inject

open class BIWFAppTest :BIWFApp(){

    @Inject
    lateinit var apiServices: ApiServices

    fun setUp() {
       // val component =DaggerTestApplicationComponent.builder()
    }
}