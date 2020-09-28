package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.POST

// TODO - This interface should be removed when our CloudCheck/Assia URLs have been updated
//  with the Apigee-passthrough versions
interface AssiaTokenService {

    //TODO - Remove after CloudCheck URLs updated since the Apigee token can be used instead
    @POST(EnvironmentPath.API_ASIA_ACCESSTOKEN_PATH)
    suspend fun getAssiaToken(): AssiaServiceResult<AssiaToken>
}
