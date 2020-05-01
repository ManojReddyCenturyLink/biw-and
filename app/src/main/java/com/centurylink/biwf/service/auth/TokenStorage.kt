package com.centurylink.biwf.service.auth

import io.reactivex.rxjava3.core.Flowable

/**
 * Handles the storage of the (JWT) token.
 *
 * @param S The type of the instance, of the JWT Token, that is handles by this storage.
 */
interface TokenStorage<S> {
    /**
     * The state of the current JWT Token.
     *
     * Reading the value of this property will read it from storage.
     *
     * Assigning a value to this property will write it to storage
     */
    var state: S?

    /**
     * This is used during a sign-in/sign-up flow to store the current policy of that flow.
     */
    var currentPolicy: String?

    /**
     * A Flowable that emits a value each time the current [state] changes from
     * having a Token (`true` is emitted) to not having one (`false` is emitted).
     */
    val hasToken: Flowable<Boolean>
}

/**
 * Updates and writes the provided state after it has been modified by the [update] lambda
 *
 * The state of type [S] should be implemented by a mutable class; it will be mutated by
 * the [update] lambda.
 */
internal fun <S> TokenStorage<S>.updateAndCommit(update: S.() -> Unit) {
    val internalState = state
    internalState?.also {
        it.update()
        state = it
    }
}

/**
 * Returns the String as a request-parameter for a policy.
 */
internal fun TokenStorage<*>.createPolicyParam() = currentPolicy.let {
    if (it.isNullOrEmpty()) emptyMap() else mapOf("p" to it)
}
