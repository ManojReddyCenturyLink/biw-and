package com.centurylink.biwf.service.impl.network

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject

/**
 * A [Call.Factory] (i.e. an OkHttpClient) that provides no authentication/authorization.
 */
class NoAuthHttpClient @Inject constructor() : Call.Factory {
    private val client by lazy {
        OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLogger { Timber.d(it) })
            .build()
    }

    override fun newCall(request: Request): Call = client.newCall(request)
}
