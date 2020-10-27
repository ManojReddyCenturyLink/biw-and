package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.Either
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

private typealias ResponseConverter<T> = Converter<ResponseBodyWithResponse?, T>

/**
 * Handles the successful responses, converting the [Either.Right] values.
 */
class EitherConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (getRawType(type) != Call::class.java) return null
        type as ParameterizedType

        val responseType = getParameterUpperBound(0, type)
        if (getRawType(responseType) != Either::class.java) return null
        responseType as ParameterizedType

        // Get the Either<*,R> type, type-parameter at index 1
        val rightType = getParameterUpperBound(1, responseType)
        val rightConverter = retrofit.responseBodyConverter<Any>(rightType, annotations)

        return EitherConverter(rightConverter)
    }
}

/**
 * Handles the failed responses, converting the [Either.Left] values.
 */
class EitherCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        returnType as ParameterizedType

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != Either::class.java) return null
        responseType as ParameterizedType

        // Get the Either<L,*> type, type-parameter at index 0
        val leftType = getParameterUpperBound(0, responseType)
        val leftConverter = retrofit.responseBodyConverter<Any>(leftType, annotations)

        @Suppress("UNCHECKED_CAST")
        return EitherCallAdapter(returnType, leftConverter as ResponseConverter<*>)
    }
}

/**
 * This is a [ResponseBody] that also can access the original [Response] from which
 * it came, so that it has access to http-response status-code, headers, etc, when needed.
 */
class ResponseBodyWithResponse(
    val response: Response<*>,
    private val delegate: ResponseBody
) : ResponseBody() {
    override fun contentLength(): Long = delegate.contentLength()

    override fun contentType(): MediaType? = delegate.contentType()

    override fun source(): BufferedSource = delegate.source()
}

private class EitherConverter(
    private val rightConverter: Converter<ResponseBody, *>
) : Converter<ResponseBody, Either<*, *>> {

    override fun convert(value: ResponseBody): Either<*, *>? {
        return rightConverter.convert(value)?.let { Either.Right(it) }
    }
}

private class EitherCallAdapter(
    private val callType: Type,
    private val leftConverter: ResponseConverter<*>
) : CallAdapter<Either<*, *>, Call<Either<*, *>>> {

    override fun responseType(): Type = callType

    override fun adapt(call: Call<Either<*, *>>): Call<Either<*, *>> {
        return EitherCall(call, leftConverter)
    }
}

private class EitherCall(
    private val call: Call<Either<*, *>>,
    private val leftConverter: ResponseConverter<*>
) : Call<Either<*, *>> by call {

    override fun enqueue(callback: Callback<Either<*, *>>) {
        call.enqueue(object : Callback<Either<*, *>> {
            override fun onResponse(call: Call<Either<*, *>>, response: Response<Either<*, *>>) {
                callback.onResponse(this@EitherCall, response.asEither)
            }

            override fun onFailure(call: Call<Either<*, *>>, t: Throwable) {
                when (t) {
                    is Error -> {
                        Timber.e("Failure Error from API")
                        callback.onFailure(call, t)
                    }
                    else -> callback.onResponse(this@EitherCall, t.asEither)
                }
            }
        })
    }

    override fun execute(): Response<Either<*, *>> = try {
        call.execute().asEither
    } catch (e: Error) {
        throw e
    } catch (t: Throwable) {
        t.asEither
    }

    private val Response<Either<*, *>>.asEither: Response<Either<*, *>>
        get() {
            val eitherBody = body()

            val errorBody = errorBody()?.let { ResponseBodyWithResponse(this, it) }

            val eitherResult = when {
                eitherBody != null -> eitherBody
                errorBody != null -> {
                    Either.Left(leftConverter.convert(errorBody))
                }
                else -> Either.Right(Unit)
            }

            return Response.success(eitherResult, headers())
        }

    @Suppress("unused")
    private val Throwable.asEither: Response<Either<*, *>>
        get() {
            Timber.e(this)

            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            val eitherBody = Either.Left(leftConverter.convert(null))

            return Response.success(eitherBody)
        }
}
