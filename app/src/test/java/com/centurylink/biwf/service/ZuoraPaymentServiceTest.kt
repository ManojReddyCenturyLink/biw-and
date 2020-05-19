package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.service.network.ZuoraPaymentService
import org.junit.Before

class ZuoraPaymentServiceTest: BaseServiceTest() {
    private lateinit var zuoraService: ZuoraPaymentService

    @Before
    fun setup() {
        createServer()
        zuoraService = retrofit.create(ZuoraPaymentService::class.java)
    }
}