package com.centurylink.biwf.service
import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.wifi.UpdateNetworkResponse
import com.centurylink.biwf.service.network.WifiStatusService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WifiStatusServiceTest : BaseServiceTest() {
    private lateinit var wifiStatusService: WifiStatusService

    @Before
    fun setup() {
        createServer()
        wifiStatusService = retrofit.create(WifiStatusService::class.java)
    }

    @Test
    fun testEnableNetworkSuccess() = runBlocking {
        enqueueResponse("apigee_enable_response.json")
        val posts: AssiaServiceResult<UpdateNetworkResponse> = wifiStatusService.enableNetwork(
            emptyMap())
        Assert.assertEquals(posts.map { it.code }, Either.Right("1000"))
        Assert.assertEquals(posts.map { it.message }, Either.Right("Success"))
    }

    @Test
    fun testdisableNetworkSuccess() = runBlocking {
        enqueueResponse("apigee_disable_response.json")
        val posts: AssiaServiceResult<UpdateNetworkResponse> = wifiStatusService.disableNetwork(
            emptyMap())
        Assert.assertEquals(posts.map { it.code }, Either.Right("1000"))
        Assert.assertEquals(posts.map { it.message }, Either.Right("Success"))
    }
}
