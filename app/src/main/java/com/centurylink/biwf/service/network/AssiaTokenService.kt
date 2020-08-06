package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.POST

// TODO - This interface should be removed when our CloudCheck/Assia URLs have been updated
//  with the Apigee-passthrough versions
interface AssiaTokenService {

    //TODO - Remove after CloudCheck URLs updated since the Apigee token can be used instead
    @POST("oauth/token?username=biwftest&password=BiwfTest1&client_id=spapi&client_secret=oBj2xZc&grant_type=password")
    suspend fun getAssiaToken(): AssiaNetworkResponse<AssiaToken, AssiaError>
}
