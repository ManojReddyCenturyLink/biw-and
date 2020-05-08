package com.centurylink.biwf.model

import com.centurylink.biwf.Either
import com.google.gson.annotations.SerializedName

typealias FiberServiceResult<T> = Either<FiberHttpError, T>

/**
 * Represents an http error (http-response status >=400).
 *
 * @property status The HTTP Response status-code.
 * @property errors The list of errors reported by Salesforce service.
 */
data class FiberHttpError(
    val status: Int = 0,
    val errors: List<FiberErrorMessage> = emptyList()
) {
    /**
     * First error-message, if any.
     */
    val message: FiberErrorMessage? get() = errors.firstOrNull()
}

/**
 * Salesforce Fiber services return error information as a JSON array
 * of JSON objects and each has two string-field called "errorCode" and "message".
 */
data class FiberErrorMessage(
    @SerializedName("errorCode")
    val errorCode: String,
    @SerializedName("message")
    val message: String
)
