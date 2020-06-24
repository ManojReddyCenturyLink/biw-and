package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.user.UpdatedPassword
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.service.network.UserService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UserServiceTest : BaseServiceTest() {

    private lateinit var userService: UserService

    @Before
    fun setup() {
        createServer()
        userService = retrofit.create(UserService::class.java)
    }

    @Test
    fun testGetUserInfoSuccess() = runBlocking {
        enqueueResponse("user.json")
        val posts: FiberServiceResult<UserInfo> = userService.qetUserInfo()
        Assert.assertEquals(
            posts.map { it.recentItems.get(0).Id },
            Either.Right("005f0000004654oAAA")
        )
        Assert.assertEquals(
            posts.map { it.recentItems.get(0).name },
            Either.Right("Pravin Kumar")
        )
    }

    @Test
    fun testGetUserInfoFailure() = runBlocking {
        val posts: FiberServiceResult<UserInfo> = userService.qetUserInfo()
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testGetUsercompleteDetailsFailure() = runBlocking {
        val posts: FiberServiceResult<UserDetails> = userService.getCompleteUserDetails("1234444")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testGetUsercompleteDetailsSuccess() = runBlocking {
        enqueueResponse("userdetails.json")
        val posts: FiberServiceResult<UserDetails> = userService.getCompleteUserDetails("1234444")
        Assert.assertEquals(posts.map { it.contactId }, Either.Right("003f000001Q5bRAAAZ"))
        Assert.assertEquals(posts.map { it.accountId }, Either.Right("001f000001RCofwAAD"))
    }

    @Test
    fun testUpdatePasswordSuccess() = runBlocking {
        val posts: FiberServiceResult<Unit> =
            userService.updatePassword("1234444", UpdatedPassword("123344"))
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}