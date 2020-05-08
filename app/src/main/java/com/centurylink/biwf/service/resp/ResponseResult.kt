package com.centurylink.biwf.service.resp

sealed class ResponseResult<out T, out E>
data class Success<T>(val value: T) : ResponseResult<T, Nothing>()
data class Failure<ErrorResponse>(val error: ErrorResponse) : ResponseResult<Nothing, ErrorResponse>()

fun <T> ResponseResult<T, *>.orElse(other: T) = if (this is Success) value else other
fun <C, T> ResponseResult<T, *>.map(f: (T) -> C) = if (this is Success) Success(f(value)) else this
fun <C, T> ResponseResult<T, *>.flatMap(f: (T) -> ResponseResult<C, *>) = if (this is Success) f(value) else this
fun <E, C> ResponseResult<*, E>.mapFailure(f: (E) -> C) = if (this is Failure) Failure(f(error)) else this

fun <T, E> ResponseResult<T, E>.orElse(f: (E) -> T) = when (this) {
    is Success -> value
    is Failure -> f(error)
}

