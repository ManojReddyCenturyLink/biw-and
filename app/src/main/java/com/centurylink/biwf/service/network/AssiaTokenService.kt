package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.POST

// TODO - This interface should be removed when the Apigee integration is complete
interface AssiaTokenService {

    //Todo this is a temporary call and will be removed post-Apigee
    @POST("oauth/token?username=biwftest&password=BiwfTest1&client_id=spapi&client_secret=oBj2xZc&grant_type=password")
    suspend fun getAssiaToken(): AssiaNetworkResponse<AssiaToken, AssiaError>
}
