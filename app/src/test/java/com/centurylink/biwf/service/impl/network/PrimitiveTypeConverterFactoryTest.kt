package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.model.primitive.CurrencyAmount
import com.centurylink.biwf.model.primitive.EmailAddress
import com.centurylink.biwf.model.primitive.PhoneNumber
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit

class PrimitiveTypeConverterFactoryTest {
    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(PrimitiveTypeConverterFactory())
        .create()

    @Test
    fun `Local Date`() {
        val expectedResult = LocalDate.of(2000, 2, 10)
        val expectedString = "2000-02-10"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, LocalDate::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Local Time with nanos`() {
        val expectedResult = LocalTime.of(19, 34, 33, 123456789)
        val expectedString = "19:34:33.123"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, LocalTime::class.java)
        assertThat(LocalTime.of(19, 34, 33, 123000000), `is`(result))
    }

    @Test
    fun `Local Time no nanos`() {
        val expectedResult = LocalTime.of(19, 34, 33)
        val expectedString = "19:34:33"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, LocalTime::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Local Date and Time`() {
        val expectedResult = LocalDateTime.of(2000, 2, 10, 19, 34, 33, 123456789)
        val expectedString = "2000-02-10T19:34:33.123"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, LocalDateTime::class.java)
        assertThat(LocalDateTime.of(2000, 2, 10, 19, 34, 33, 123000000), `is`(result))
    }

    @Test
    fun `Date and Time and TimeZone Z`() {
        val expectedResult = ZonedDateTime.of(2000, 2, 10, 19, 34, 33, 123000000, ZoneOffset.UTC)
        val expectedString = "2000-02-10T19:34:33.123Z"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, ZonedDateTime::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Date and Time and TimeZone +1`() {
        val expectedResult =
            ZonedDateTime.of(2000, 2, 10, 19, 34, 33, 123000000, ZoneOffset.ofHours(1))
        val expectedString = "2000-02-10T19:34:33.123+01:00"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, ZonedDateTime::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Duration in milliseconds`() {
        val expectedResult = Duration.of(345, ChronoUnit.MILLIS)
        val expectedString = "345"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.isNumber, `is`(true))
        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, Duration::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Currency Amounts no cents`() {
        val expectedResult = CurrencyAmount(2334)
        val expectedString = "2334.00"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.isNumber, `is`(true))
        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, CurrencyAmount::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Currency Amounts some cents`() {
        val expectedResult = CurrencyAmount(0.02)
        val expectedString = "0.02"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.isNumber, `is`(true))
        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, CurrencyAmount::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Currency Amounts with one decimal digit`() {
        val expectedResult = CurrencyAmount("38247190.2")
        val expectedString = "38247190.20"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.isNumber, `is`(true))
        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, CurrencyAmount::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Email Address`() {
        val expectedResult = EmailAddress("anton@accenture.com")
        val expectedString = "anton@accenture.com"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, EmailAddress::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Phone Number`() {
        val expectedResult = PhoneNumber("6175559903")
        val expectedString = "6175559903"

        val json = gson.toJsonTree(expectedResult) as JsonPrimitive

        assertThat(json.asString, `is`(expectedString))

        val result = gson.fromJson(json, PhoneNumber::class.java)
        assertThat(expectedResult, `is`(result))
    }

    @Test
    fun `Lenient Parsing of Date Time and TimeZone`() {
        val dateInput = "2000-02-10T19:34:33.123Z"
        val date = gson.fromJson(JsonPrimitive(dateInput), ZonedDateTime::class.java)

        val json = TestData.jsonStringFor(dateInput)
        val result = gson.fromJson(json, TestData::class.java)

        assertThat(result, `is`(TestData(date, date.toLocalDateTime(), date.toLocalDate())))
    }

    @Test
    fun `Lenient Parsing of Date Time`() {
        val dateInput = "2000-02-10T19:34:33.123"
        val date = gson.fromJson(JsonPrimitive(dateInput), LocalDateTime::class.java)

        val json = TestData.jsonStringFor(dateInput)
        val result = gson.fromJson(json, TestData::class.java)

        assertThat(result, `is`(TestData(date.atZone(ZoneId.of("Z")), date, date.toLocalDate())))
    }

    @Test
    fun `Lenient Parsing of Date`() {
        val dateInput = "2000-02-10"
        val date = gson.fromJson(JsonPrimitive(dateInput), LocalDate::class.java)

        val json = TestData.jsonStringFor(dateInput)
        val result = gson.fromJson(json, TestData::class.java)

        assertThat(
            result,
            `is`(TestData(date.atStartOfDay(ZoneId.of("Z")), date.atStartOfDay(), date))
        )
    }

    private data class TestData(
        val dateTimeZone: ZonedDateTime,
        val dateTime: LocalDateTime,
        val date: LocalDate
    ) {
        companion object {
            fun jsonStringFor(dateInput: String): String {
                return """
                    {
                        "dateTimeZone": "$dateInput",
                        "dateTime": "$dateInput",
                        "date": "$dateInput"
                    }
                """.trimIndent()
            }
        }
    }
}
