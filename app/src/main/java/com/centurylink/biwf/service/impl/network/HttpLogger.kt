package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Simple logger for an installed OkHttpClient, showing the full body of HTTP requests and
 * responses. It only logs information on DEBUG builds.
 *
 * @param logger Function that provides the actual logging.
 */
class HttpLogger private constructor(logger: (String) -> Unit) : (Interceptor.Chain) -> Response {
    private val logger: HttpLoggingInterceptor.Logger = object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            logger(message)
        }
    }

    override operator fun invoke(chain: Interceptor.Chain): Response {
        val httpLogging = HttpLoggingInterceptor(logger).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        @Suppress("ConstantConditionIf")
        return if (BuildConfig.DEBUG) {
            httpLogging.intercept(chain)
        } else {
            chain.proceed(chain.request())
        }
    }

    companion object {
        operator fun invoke(logger: (String) -> Unit): HttpLogger = HttpLogger(logger)
    }
}
