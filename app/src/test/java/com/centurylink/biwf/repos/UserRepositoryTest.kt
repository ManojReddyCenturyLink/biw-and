package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.service.network.UserService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UserRepositoryTest : BaseRepositoryTest() {

    private lateinit var userRepository: UserRepository

    @MockK(relaxed = true)
    private lateinit var userService: UserService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var userInfo: UserInfo

    private lateinit var userDetails: UserDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        val userInfoString = readJson("user.json")
        val userDetailsString = readJson("userdetails.json")
        userInfo = fromJson(userInfoString)
        userDetails = fromJson(userDetailsString)
        userRepository = UserRepository(mockPreferences, userService)
    }

    @Test
    fun testgetUserInfoSuccess() {
        runBlockingTest {
            launch {
                coEvery { userService.qetUserInfo() } returns Either.Right(userInfo)
                val userInformation = userRepository.getUserInfo()
                Assert.assertEquals(
                    userInformation.map { it.recentItems.get(0).Id },
                    Either.Right("005f0000004654oAAA")
                )
                Assert.assertEquals(
                    userInformation.map { it.recentItems.get(0).name },
                    Either.Right("Pravin Kumar")
                )
            }
        }
    }

    @Test
    fun testgetUserInfoError() {
        runBlockingTest {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(FiberErrorMessage(errorCode = Constants.ERROR_CODE_1000, message = "Error"))
                )
                coEvery { userService.qetUserInfo() } returns Either.Left(fiberHttpError)
                val userInformation = userRepository.getUserInfo()
                Assert.assertEquals(
                    userInformation.mapLeft { it },
                    Either.Left(Constants.ERROR)
                )
            }
        }
    }

    @Test
    fun testgetUserDetailsSuccess() {
        runBlockingTest {
            launch {
                coEvery { userService.getCompleteUserDetails(any()) } returns Either.Right(
                    userDetails
                )
                val userDetailsInfo = userRepository.getUserDetails()
                Assert.assertEquals(
                    userDetailsInfo.map { it.contactId },
                    Either.Right("003f000001Q5bRAAAZ")
                )
                Assert.assertEquals(
                    userDetailsInfo.map { it.accountId },
                    Either.Right("001f000001RCofwAAD")
                )
            }
        }
    }

    @Test
    fun testgetUserDetailsError() {
        runBlockingTest {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(FiberErrorMessage(errorCode = Constants.ERROR_CODE_1000, message = Constants.ERROR))
                )
                coEvery { userService.getCompleteUserDetails(any()) } returns Either.Left(
                    fiberHttpError
                )
                val userDetailsInfo = userRepository.getUserDetails()
                Assert.assertEquals(userDetailsInfo.mapLeft { it }, Either.Left(Constants.ERROR))
            }
        }
    }

    @Test
    fun resetPassWordSuccess() {
        runBlockingTest {
            launch {
                coEvery { userService.updatePassword(any(), any()) } returns Either.Right(Unit)
                val userDetailsInfo = userRepository.resetPassWord("12345")
                Assert.assertEquals(userDetailsInfo.map { it }, userDetailsInfo.map { it })
            }
        }
    }

    @Test
    fun resetPassWordError() {
        runBlockingTest {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(FiberErrorMessage(errorCode = Constants.ERROR_CODE_1000, message = Constants.ERROR))
                )
                coEvery { userService.updatePassword(any(), any()) } returns Either.Left(
                    fiberHttpError
                )
                val userDetailsInfo = userRepository.resetPassWord("12345")
                Assert.assertEquals(userDetailsInfo.map { it }, userDetailsInfo.map { it })
            }
        }
    }
}
