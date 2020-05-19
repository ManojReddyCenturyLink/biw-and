package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.service.network.UserService
import org.junit.Before

class UserServiceTest : BaseServiceTest(){
    private lateinit var userService: UserService
    @Before
    fun setup() {
        createServer()
        userService = retrofit.create(UserService::class.java)
    }
}