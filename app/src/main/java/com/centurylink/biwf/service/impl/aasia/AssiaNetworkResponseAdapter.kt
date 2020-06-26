package com.centurylink.biwf.service.impl.aasia

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

class AssiaNetworkResponseAdapter<S : Any, E : Any>(
    private val successType: Type,
    private val errorBodyConverter: Converter<ResponseBody, E>
) : CallAdapter<S, Call<AssiaNetworkResponse<S, E>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<S>): Call<AssiaNetworkResponse<S, E>> {
        return AssiaNetworkResponseCall(call, errorBodyConverter)
    }
}
