package com.centurylink.biwf.model

import com.centurylink.biwf.Either
import com.google.gson.annotations.SerializedName


typealias AssiaServiceResult<T> = Either<AssiaHttpError, T>

/**
 * Represents an http error (http-response status >=400).
 *
 * @property status The HTTP Response status-code.
 * @property errors The list of errors reported by Assia service.
 */
data class AssiaHttpError(
    val status: Int = 0,
    val errors: AssiaErrorMessage = AssiaErrorMessage("","")
) {
    /**
     * First error-message, if any.
     */
    val message: AssiaErrorMessage? get() = errors
}

/**
 * Assia services return error information as a JSON array
 * of JSON objects and each has two string-field called "errorCode" and "message".
 */
//{"code":1603,"message":"User does not have the permission to invoke this API","data":null}
data class AssiaErrorMessage(
    @SerializedName("code")
    val error: String,

    @SerializedName("message")
    val message: String
)
