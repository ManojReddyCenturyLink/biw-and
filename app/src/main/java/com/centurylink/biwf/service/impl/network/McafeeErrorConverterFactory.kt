package com.centurylink.biwf.service.impl.network


import com.centurylink.biwf.model.McafeeErrorMessage
import com.centurylink.biwf.model.McafeeHttpError
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType

private typealias McafeeErrorMessages = McafeeErrorMessage

/**
 * Converts [McafeeHttpError] from incoming JSON from the server when the server
 * returns an unsuccessful HTTP Response.
 */
class McafeeErrorConverterFactory : Converter.Factory() {
    @ExperimentalStdlibApi
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (getRawType(type) != McafeeHttpError::class.java) return null

        val errorMessageConverter =
            retrofit.responseBodyConverter<McafeeErrorMessages>(
                McafeeHttpError::errors.returnType.javaType,
                annotations
            )

        return McafeeErrorConverter(errorMessageConverter)
    }
}

private class McafeeErrorConverter(
    private val errorMessagesConverter: Converter<ResponseBody, McafeeErrorMessages>
) : Converter<ResponseBody, McafeeHttpError> {

    override fun convert(value: ResponseBody?): McafeeHttpError? {
        value as ResponseBodyWithResponse?

        if (value == null) return McafeeHttpError(
            status = 0
        )

        return errorMessagesConverter.convert(value)?.let {
            McafeeHttpError(
                status = value.response.code(),
                errors = it
            )
        } ?: McafeeHttpError(status = value.response.code())
    }
}
