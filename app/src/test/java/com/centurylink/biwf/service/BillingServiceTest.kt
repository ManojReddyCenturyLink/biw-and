package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.service.network.BillingApiServices
import org.junit.Before

class BillingServiceTest : BaseServiceTest() {

    private lateinit var billingApiServices: BillingApiServices

    @Before
    fun setup() {
        createServer()
        billingApiServices = retrofit.create(BillingApiServices::class.java)
    }
}
