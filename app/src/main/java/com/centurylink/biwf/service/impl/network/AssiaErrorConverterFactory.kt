package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.model.AssiaErrorMessage
import com.centurylink.biwf.model.AssiaHttpError
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType

private typealias AssiaErrorMessages = AssiaErrorMessage

/**
 * Converts [AssiaHttpError] from incoming JSON from the server when the server
 * returns an unsuccessful HTTP Response.
 */
class AssiaErrorConverterFactory : Converter.Factory() {
    @ExperimentalStdlibApi
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (getRawType(type) != AssiaHttpError::class.java) return null

        val errorMessageConverter =
            retrofit.responseBodyConverter<AssiaErrorMessages>(
                AssiaHttpError::errors.returnType.javaType,
                annotations
            )

        return AssiaErrorConverter(errorMessageConverter)
    }
}

private class AssiaErrorConverter(
    private val errorMessagesConverter: Converter<ResponseBody, AssiaErrorMessages>
) : Converter<ResponseBody, AssiaHttpError> {

    override fun convert(value: ResponseBody?): AssiaHttpError? {
        value as ResponseBodyWithResponse?
        if (value == null) return AssiaHttpError(
            status = 0
        )
        return errorMessagesConverter.convert(value)?.let {
            AssiaHttpError(
                status = value.response.code(), errors = it
            )
        } ?: AssiaHttpError(status = value.response.code())
    }
}
