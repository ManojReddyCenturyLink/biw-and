package com.centurylink.biwf.service.impl.network

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * A [Call.Factory] (i.e. an OkHttpClient) that provides no authentication/authorization.
 */
class NoAuthHttpClient @Inject constructor() : Call.Factory {
    private val client by lazy {
        OkHttpClient.Builder()
            .readTimeout(30000, TimeUnit.MILLISECONDS)
            .connectTimeout(30000, TimeUnit.MILLISECONDS)
            .writeTimeout(30000, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(HttpLogger { Timber.d(it) })
            .build()
    }

    override fun newCall(request: Request): Call = client.newCall(request)
}
