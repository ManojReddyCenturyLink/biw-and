package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.service.auth.TokenService
import com.centurylink.biwf.service.auth.accessTokenHeader
import com.centurylink.biwf.service.integration.IntegrationServerService
import okhttp3.Authenticator
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.internal.http.RealResponseBody
import okio.Buffer
import timber.log.Timber
import javax.inject.Inject

/**
 * A [Call.Factory] (i.e. an OkHttpClient) that provides the necessary access-token for
 * each Http request made through this client.
 *
 * @property tokenService The service that provides this client/factory with access-tokens.
 */
class OAuthHttpClient @Inject constructor(
    private val tokenService: TokenService,
    private val integrationServerService: IntegrationServerService
) : Call.Factory {

    private val client by lazy {
        OkHttpClient.Builder()
            .apply {
                addInterceptor {
                    integrationServerService.start()
                    it.proceed(it.request())
                }
            }
            .addInterceptor { addAccessTokenHeader(tokenService, it) }
            .authenticator(retryWithNewAccessToken(tokenService))
            .addNetworkInterceptor(HttpLogger { Timber.d(it) })
            .build()
    }

    override fun newCall(request: Request): Call = client.newCall(request)
}

/**
 * Fetches a fresh access-token from the [service] and adds it to the request in the [chain].
 */
private fun addAccessTokenHeader(service: TokenService, chain: Interceptor.Chain): Response {
    val accessTokenHeader = service.accessTokenHeader
    return if (accessTokenHeader.isNotEmpty()) {
        val request = chain.request()
            .newBuilder()
            .addHeader(ACCEPT_HEADER_NAME, ACCEPT_HEADER_ALL)
            .removeHeader(AUTH_HEADER_NAME)
            .addHeader(AUTH_HEADER_NAME, accessTokenHeader)
            .tag(0)
            .build()

        chain.proceed(request)
    } else {
        service.invalidateToken()

        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(HTTP_CODE_UNAUTHORIZED)
            .message("Access-token could not be obtained")
            .body(RealResponseBody("application/json; charset=utf-8", 0, Buffer()))
            .build()
    }
}

/**
 * If a 401 was received, try once more, but invalidate the access-token before retrying.
 */
private fun retryWithNewAccessToken(service: TokenService): Authenticator = object : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        var retryCount = (request.tag() as? Int) ?: Int.MAX_VALUE

        return if (retryCount < MAX_AUTH_RETRIES) {
            val accessTokenHeader = service.let {
                it.invalidateToken()
                it.accessTokenHeader
            }

            if (accessTokenHeader.isNotEmpty()) {
                request
                    .newBuilder()
                    .removeHeader(AUTH_HEADER_NAME)
                    .addHeader(AUTH_HEADER_NAME, accessTokenHeader)
                    .tag(++retryCount)
                    .build()
            } else {
                null
            }
        } else {
            // All bets are off. Clear both access- and refresh-token.
            service.clearToken()

            null
        }
    }
}

private const val ACCEPT_HEADER_NAME = "Accept"
private const val ACCEPT_HEADER_ALL = "*/*"
private const val AUTH_HEADER_NAME = "Authorization"
private const val HTTP_CODE_UNAUTHORIZED = 401
private const val MAX_AUTH_RETRIES = 1
