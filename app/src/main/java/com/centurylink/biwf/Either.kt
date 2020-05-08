package com.centurylink.biwf

import com.centurylink.biwf.Either.Left
import com.centurylink.biwf.Either.Right

/**
 * Represents a result of a computation.
 *
 * The result is either a [Right] of type [T] or it is a [Left] of type [E].
 *
 * [Right] represents 'good'/'successful' results, while [Left] represents errors.
 */
sealed class Either<out E, out T> {
    /**
     * Successful result of type [T].
     * The result is stored in [value].
     */
    data class Right<out T>(val value: T) : Either<Nothing, T>()

    /**
     * Failed result of type [E].
     * The result is stored in [error].
     */
    data class Left<out E>(val error: E) : Either<E, Nothing>()

    /**
     * Maps a [Right] of type [T] to a [Right] of type [T2], if this is a [Right].
     *
     * If this is a [Left], return this [Left] instead.
     */
    inline fun <T2> map(ifRight: (T) -> T2): Either<E, T2> =
        when (this) {
            is Right -> Right(ifRight(value))
            is Left -> this
        }

    /**
     * Maps a [Left] of type [E] to a [Left] of type [E2], if this is [Left]
     *
     * If this is a [Right], return this [Right] instead.
     */
    inline fun <E2> mapLeft(ifLeft: (E) -> E2): Either<E2, T> =
        when (this) {
            is Right -> this
            is Left -> Left(ifLeft(error))
        }

    /**
     * Maps a [Right] of type [T] to a [Right] of type [T2], if this is a [Right]
     *
     *  otherwise
     *
     * Maps a [Left] of type [E] to a [Left] of type [E2]
     */
    inline fun <E2, T2> bimap(ifLeft: (E) -> E2, ifRight: (T) -> T2): Either<E2, T2> =
        fold({ Left(ifLeft(it)) }, { Right(ifRight(it)) })

    /**
     * If this [Either] is a [Right], the [ifRight] is called with the [Right.value].
     *
     * If this [Either] is a [Left], the [ifLeft] is called with the [Left.error].
     *
     * This function then will return the result either of the two provided lambdas.
     */
    inline fun <T2> fold(ifLeft: (E) -> T2, ifRight: (T) -> T2): T2 = when (this) {
        is Right -> ifRight(value)
        is Left -> ifLeft(error)
    }

    /**
     * If this is a [Right] value, returns its [Right.value], otherwise returns `null`.
     */
    inline fun optional(): T? = fold({ null }, { it })
}

/**
 * Maps a [Right] of type [T] to an [Either] of type [T2], if this is a [Right] by returning
 * the result of [ifRight].
 *
 * If this is [Left], return this [Left] instead.
 */
inline fun <E, T, T2> Either<E, T>.flatMap(ifRight: (T) -> Either<E, T2>): Either<E, T2> =
    when (this) {
        is Right -> ifRight(value)
        is Left -> this
    }
