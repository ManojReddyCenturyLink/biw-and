package com.centurylink.biwf.service.impl.network

import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Creates [Retrofit] instances that uses the provided http [callFactory]/client
 * to transport HTTP requests and responses.
 */
class RetrofitFactory @Inject constructor(
    private val callFactory: okhttp3.Call.Factory,
    private val converter: Converter.Factory,
    private val callAdapter: CallAdapter.Factory
) {
    fun create(baseUrl: String): Retrofit = Retrofit.Builder()
        .callFactory(callFactory)
        .baseUrl(baseUrl)
        .addConverterFactory(converter)
        .addCallAdapterFactory(callAdapter)
        .build()
}

/**
 * Synthetic sugaring to create Retrofit Service.
 */
inline fun <reified T> Retrofit.create(): T = create(T::class.java)
