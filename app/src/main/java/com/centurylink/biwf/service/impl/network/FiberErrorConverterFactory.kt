package com.centurylink.biwf.service.impl.network

import android.util.Log
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType

private typealias ErrorMessages = List<FiberErrorMessage>

/**
 * Converts [FiberHttpError] from incoming JSON from the server when the server
 * returns an unsuccessful HTTP Response.
 */
class FiberErrorConverterFactory : Converter.Factory() {
    @ExperimentalStdlibApi
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (getRawType(type) != FiberHttpError::class.java) return null

        val errorMessageConverter =
            retrofit.responseBodyConverter<ErrorMessages>(
                FiberHttpError::errors.returnType.javaType,
                annotations
            )

        return FiberErrorConverter(errorMessageConverter)
    }
}

private class FiberErrorConverter(
    private val errorMessagesConverter: Converter<ResponseBody, ErrorMessages>
) : Converter<ResponseBody, FiberHttpError> {

    override fun convert(value: ResponseBody?): FiberHttpError? {
        value as ResponseBodyWithResponse?

        if (value == null) return FiberHttpError(status = 0)

        return errorMessagesConverter.convert(value)?.let {
            FiberHttpError(status = value.response.code(), errors = it)
        } ?: FiberHttpError(status = value.response.code())
    }
}
