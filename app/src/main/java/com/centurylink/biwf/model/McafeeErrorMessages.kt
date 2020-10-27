package com.centurylink.biwf.model

import com.centurylink.biwf.Either
import com.google.gson.annotations.SerializedName

typealias McafeeServiceResult<T> = Either<McafeeHttpError, T>

/**
 * Represents an http error (http-response status >=400).
 *
 * @property status The HTTP Response status-code.
 * @property errors The list of errors reported by Mcafee service.
 */
data class McafeeHttpError(
    val status: Int = 0,
    val errors: McafeeErrorMessage? = null
) {
    /**
     * First error-message, if any.
     */
    val message: McafeeErrorMessage? get() = errors
}

/**
 * Mcafee services return error information as a JSON array
 * of JSON objects and each has two string-field called "errorCode" and "message".
 */
data class McafeeErrorMessage(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String
)
