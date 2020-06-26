package com.centurylink.biwf.service.impl.aasia

import java.io.IOException

sealed class AssiaNetworkResponse<out T : Any, out U : Any> {
    /**
     * Success response with body
     */
    data class Success<T : Any>(val body: T) : AssiaNetworkResponse<T, Nothing>()

    /**
     * Failure response with body
     */
    data class ApiError<U : Any>(val body: U, val code: Int) : AssiaNetworkResponse<Nothing, U>()

    /**
     * Network error
     */
    data class AssiaNetworkError(val error: IOException) : AssiaNetworkResponse<Nothing, Nothing>()

    /**
     * For example, json parsing error
     */
    data class UnknownError(val error: Throwable?) : AssiaNetworkResponse<Nothing, Nothing>()
}
