package com.centurylink.biwf.screens.model

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Test

class StringResourceTest {
    private val mockResources: Resources = mockk(relaxed = true) {
        every { getQuantityString(any(), any()) } answers {
            it.invocation.args.asString("plural")
        }

        every { getQuantityString(any(), any(), any()) } answers {
            it.invocation.args.asString("plural")
        }

        every { getQuantityString(any(), any(), any(), any()) } answers {
            it.invocation.args.asString("plural")
        }

        every { getQuantityString(any(), any(), any(), any(), any()) } answers {
            it.invocation.args.asString("plural")
        }
    }

    private val mockContext: Context = mockk(relaxed = true) {
        every { resources } answers {
            mockResources
        }

        every { getString(any()) } answers {
            it.invocation.args.asString("string")
        }

        every { getString(any(), any()) } answers {
            it.invocation.args.asString("string")
        }

        every { getString(any(), any(), any()) } answers {
            it.invocation.args.asString("string")
        }

        every { getString(any(), any(), any(), any()) } answers {
            it.invocation.args.asString("string")
        }
    }

    @Test
    fun `StringResource that is empty`() {
        assertThat(StringResource().getUiString(mockContext), `is`(""))
    }

    @Test
    fun `EMPTY StringResource is empty`() {
        assertThat(StringResource.EMPTY.getUiString(mockContext), `is`(""))
    }

    @Test
    fun `EMPTY StringResource is same as a wrapped empty string`() {
        assertThat(StringResource.EMPTY === StringResource.wrap(""), `is`(true))
    }

    @Test
    fun `EMPTY StringResource is not the same as a wrapped blank string`() {
        assertThat(StringResource.EMPTY === StringResource.wrap(" "), `is`(false))
        assertThat(StringResource.wrap(" "), not(StringResource.EMPTY))
    }

    @Test
    fun `StringResource asStringResource wraps plain string`() {
        val expectedText = "Hello"
        assertThat(expectedText, `is`(expectedText.asStringResource.getUiString(mockContext)))
    }

    @Test
    fun `StringResource with no substitutions`() {
        assertThat(StringResource(1001).getUiString(mockContext), `is`("string-1001:"))
    }

    @Test
    fun `StringResource with a substitution`() {
        assertThat(StringResource(1000, 5).getUiString(mockContext), `is`("string-1000:5"))
    }

    @Test
    fun `StringResource with more than one substitution`() {
        assertThat(mockContext.getString(StringResource(1000, 1001, "1002")), `is`("string-1000:1001,1002"))
    }

    @Test
    fun `Plural StringResource with no substitutions`() {
        assertThat(StringResource.plural(1001, 3).getUiString(mockContext), `is`("plural-1001(3):"))
    }

    @Test
    fun `Plural StringResource with a substitution`() {
        assertThat(StringResource.plural(1000, 2, 5).getUiString(mockContext), `is`("plural-1000(2):5"))
    }

    @Test
    fun `Plural StringResource with more than one substitution`() {
        assertThat(
            mockContext.getString(StringResource.plural(1000, 2, 1001, "1002")),
            `is`("plural-1000(2):1001,1002")
        )
    }

    @Test
    fun `StringResource with embedded StringResource`() {
        assertThat(
            StringResource(1000, 345, StringResource(1001, 5, 13), 1002).getUiString(mockContext),
            `is`("string-1000:345,string-1001:5,13,1002")
        )
    }

    @Test
    fun `Plural StringResource with embedded plural StringResource`() {
        assertThat(
            StringResource.plural(1000, 1, 345, StringResource.plural(1001, 1, 5, 13), 1002).getUiString(mockContext),
            `is`("plural-1000(1):345,plural-1001(1):5,13,1002")
        )
    }

    @Test
    fun `StringResource with embedded plural StringResource`() {
        assertThat(
            StringResource(1000, 345, StringResource.plural(1001, 1, 5, 13), 1002).getUiString(mockContext),
            `is`("string-1000:345,plural-1001(1):5,13,1002")
        )
    }

    @Test
    fun `Plural StringResource with embedded StringResource`() {
        assertThat(
            StringResource.plural(1000, 2, 345, StringResource(1001, 5, 13), 1002).getUiString(mockContext),
            `is`("plural-1000(2):345,string-1001:5,13,1002")
        )
    }

    @Test
    fun `Equality of StringResources`() {
        val res1 = StringResource(1000, 345, StringResource(1001, "5", 13), 1002)
        val res2 = StringResource(1000, 345, StringResource(1001, "5", 13), 1002)
        assertThat(res2, `is`(res1))
    }

    @Test
    fun `Inequality of StringResources`() {
        val res1 = StringResource(1000, 345, StringResource(1001, "5", 13), 1002)
        val res2 = StringResource(1000, 345, StringResource(1001, 5, 13), 1002)
        assertThat(res2, not(res1))
    }

    @Test
    fun `Equality of StringResources Hashcode`() {
        val res1 = StringResource(1000, 345, StringResource(1001, 5, 13), 1002)
        val res2 = StringResource(1000, 345, StringResource(1001, 5, 13), 1002)
        assertThat(res2.hashCode(), `is`(res1.hashCode()))
    }

    @Test
    fun `Equality of plural StringResources`() {
        val res1 = StringResource.plural(1000, 2, 345, StringResource(1001, "5", 13), 1002)
        val res2 = StringResource.plural(1000, 2, 345, StringResource(1001, "5", 13), 1002)
        assertThat(res2, `is`(res1))
    }

    @Test
    fun `Inequality of plural StringResources`() {
        val res1 = StringResource.plural(1000, 2, 345, StringResource(1001, "5", 13), 1002)
        val res2 = StringResource.plural(1000, 2, 345, StringResource(1001, 5, 13), 1002)
        assertThat(res2, not(res1))
    }

    @Test
    fun `Equality of plural StringResources Hashcode`() {
        val res1 = StringResource.plural(1000, 2, 345, StringResource(1001, 5, 13), 1002)
        val res2 = StringResource.plural(1000, 2, 345, StringResource(1001, 5, 13), 1002)
        assertThat(res2.hashCode(), `is`(res1.hashCode()))
    }
}

/**
 * Creates a unique string out of a string- or plural-id and its arguments.
 * The string-/plural-id and its arguments are all encoded in the this [List] receiver.
 *
 * @param type Either 'string' or 'plural'
 */
private fun List<Any?>.asString(type: String): String {
    val lastIndexBeforeVarArgs = indexOfFirst { it is Array<*> }.let { if (it >= 0) it else size } - 1

    val flattened = mapIndexed { index, arg ->
        when (index) {
            lastIndexBeforeVarArgs + 1 -> arg as Array<*>
            else -> arrayOf(arg)
        }
    }.flatMap { it.toList() }

    val separator = { index: Int -> if (index == lastIndexBeforeVarArgs) ":" else "" }

    return flattened.foldIndexed("") { index, buffer, arg ->
        buffer + when (index) {
            0 -> "$type-${arg as Int}${separator(index)}"
            lastIndexBeforeVarArgs -> "(${arg as Int})${separator(index)}"
            flattened.size - 1 -> "$arg"
            else -> "$arg,"
        }
    }
}
