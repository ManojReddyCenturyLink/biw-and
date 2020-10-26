package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.service.network.AssiaTokenService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// TODO - This class is temporary and should be removed when the Apigee integration is complete
// (Token TTL is 1 hour, so we are currently assuming there will not be a dev session that long)
@Singleton
class AssiaTokenManager @Inject constructor(private val assiaTokenService: AssiaTokenService) {

    private var assiaToken = ""

    suspend fun getAssiaToken(): String {
        if (assiaToken.isEmpty()) {
            val response = assiaTokenService.getAssiaToken()
            response.fold(ifRight = {
                assiaToken = it.accessToken
                return assiaToken
            }, ifLeft = {
                Timber.e("Issue obtaining Assia Token")
            })
        }
        return assiaToken
    }
}
