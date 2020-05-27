package com.centurylink.biwf.screens.model

import android.content.Context
import android.view.View
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * This class represents an Android string-resource and its possible arguments.
 *
 * The 'stringId' of the [resource] and [arguments] parameters are the same that one would used when
 * calling the [Context.getString] method.
 *
 * @param resource The resource containing either a string-id or plural-id value.
 * @param arguments The arguments to be formatted into the string value of the string resource.
 */
class StringResource private constructor(
    private val resource: Resource = Simple(0),
    private vararg val arguments: Any
) {
    companion object {
        /**
         * A [StringResource] that represents an empty string.
         */
        val EMPTY = StringResource(0)

        /**
         * Wraps a plain string inside a [StringResource].
         *
         * If the provided string is empty, [EMPTY] will be returned instead.
         *
         * @param string String to wrap
         * @return A StringResource representing the wrapped string.
         */
        fun wrap(string: String) = if (string.isEmpty()) EMPTY else StringResource(0, string)

        /**
         * Creates a [StringResource] from a [stringId] and optional [arguments].
         */
        operator fun invoke(@StringRes stringId: Int = 0, vararg arguments: Any): StringResource =
            StringResource(Simple(stringId), *arguments)

        /**
         * Wraps a plurals resource inside a [StringResource]
         */
        fun plural(@PluralsRes pluralId: Int, count: Int, vararg arguments: Any): StringResource =
            StringResource(Plural(pluralId, count), *arguments)
    }

    /**
     * Please, do no call directly. Use the [Context.getString] or [Fragment.getString] extension functions instead.
     */
    internal fun getUiString(context: Context): String = resource.getUiStringHelper(context, arguments)

    override fun equals(other: Any?): Boolean {
        return other is StringResource && resource == other.resource && arguments.contentEquals(other.arguments)
    }

    override fun hashCode(): Int {
        return resource.hashCode() + 31 * arguments.contentHashCode()
    }

    override fun toString(): String {
        return if (this == EMPTY) "" else "$resource${arguments.map { it.toString() }}"
    }
}

/**
 * @return This String as a [StringResource]. See also [StringResource.wrap]
 */
val String.asStringResource: StringResource
    get() = StringResource.wrap(this)

/**
 * @return The [String] value of the [stringResource] for the `this` [Context]
 */
fun Context.getString(stringResource: StringResource): String = stringResource.getUiString(this)

/**
 * @return The [String] value of the [stringResource] for the `this` [Fragment]
 */
fun Fragment.getString(stringResource: StringResource): String = activity!!.getString(stringResource)

/**
 * @return The [String] value of the [stringResource] for the Context of `this` [View]
 */
fun View.getString(stringResource: StringResource): String = context.getString(stringResource)

private sealed class Resource {
    abstract fun getUiStringHelper(context: Context, arguments: Array<out Any>): String
}

private data class Simple(@StringRes private val stringId: Int) : Resource() {
    override fun getUiStringHelper(context: Context, arguments: Array<out Any>): String =
        when (stringId) {
            0 -> if (arguments.isEmpty()) "" else checkArguments(context, arguments)[0].toString()
            else -> if (arguments.isEmpty())
                context.getString(stringId)
            else
                context.getString(stringId, *checkArguments(context, arguments))
        }

    override fun toString(): String = "@string/$stringId:"
}

private data class Plural(
    @PluralsRes private val pluralId: Int,
    private val count: Int
) : Resource() {
    override fun getUiStringHelper(context: Context, arguments: Array<out Any>): String {
        return when (pluralId) {
            0 -> if (arguments.isEmpty()) "" else checkArguments(context, arguments)[0].toString()
            else -> if (arguments.isEmpty())
                context.resources.getQuantityString(pluralId, count)
            else
                context.resources.getQuantityString(pluralId, count, *checkArguments(context, arguments))
        }
    }

    override fun toString(): String = "@plural/$pluralId(count=$count):"
}

private fun checkArguments(context: Context, arguments: Array<out Any>): Array<out Any> {
    return Array(arguments.size) {
        arguments[it].let { arg ->
            (arg as? StringResource)?.getUiString(context) ?: arg
        }
    }
}
