package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.service.network.AccountApiService
import com.centurylink.biwf.service.network.ContactApiService
import org.junit.Before

class ContactApiServiceTest : BaseServiceTest() {

    private lateinit var contactApiService: ContactApiService

    @Before
    fun setup() {
        createServer()
        contactApiService = retrofit.create(ContactApiService::class.java)
    }
}