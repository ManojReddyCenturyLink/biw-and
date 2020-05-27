package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.model.primitive.CurrencyAmount
import com.centurylink.biwf.model.primitive.EmailAddress
import com.centurylink.biwf.model.primitive.PhoneNumber
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.threeten.bp.*
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_TIME
import org.threeten.bp.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.ResolverStyle
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.Temporal
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.Locale

/**
 * Provides converters of these 'primitive' types for Gson and Retrofit:
 *
 * * [LocalDate]: Stores a date without a time. This stores a date like '2010-12-03' and could be used to store a birthday.
 * * [LocalTime]: Stores a time of day without a date. This stores a time like '11:30' and could be used to store an opening or closing time.
 * * [LocalDateTime]: stores a date and time of day. This stores a date-time like '2010-12-03T11:30'.
 * * [ZonedDateTime]: Stores a date and time with a time-zone.
 * * [Duration]: Stores a duration as a simple number of milliseconds.
 * * [CurrencyAmount]: Stores an amount of money of a given currency (defaults to USD)
 * * [EmailAddress]: Stores an properly constructed email address.
 * * [PhoneNumber]: Stores a US phone-number.
 *
 */
class PrimitiveTypeConverterFactory : TypeAdapterFactory, Converter.Factory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? =
        when (type.rawType) {
            LocalDate::class.java -> LocalDateConverter()
            LocalTime::class.java -> LocalTimeConverter()
            LocalDateTime::class.java -> LocalDateTimeConverter()
            ZonedDateTime::class.java -> ZonedDateTimeConverter()
            Duration::class.java -> DurationConverter()
            CurrencyAmount::class.java -> CurrencyAmountConverter()
            EmailAddress::class.java -> EmailAddressConverter()
            PhoneNumber::class.java -> PhoneNumberConverter()
            else -> null
        }?.nullSafe() as TypeAdapter<T>?

    override fun stringConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit) =
        when (type) {
            LocalDate::class.java -> Conv<LocalDate> { it.format(ISO_LOCAL_DATE) }
            LocalTime::class.java -> Conv<LocalTime> { it.format(ISO_LOCAL_TIME) }
            LocalDateTime::class.java -> Conv<LocalDateTime> { it.format(ISO_LOCAL_DATE_TIME) }
            ZonedDateTime::class.java -> Conv<ZonedDateTime> { it.format(ISO_ZONED_DATE_TIME) }
            Duration::class.java -> Conv<Duration> { it.toMillis().toString() }
            CurrencyAmount::class.java -> Conv<CurrencyAmount> { it.amount.toString() }
            EmailAddress::class.java -> Conv<EmailAddress> { it.value }
            PhoneNumber::class.java -> Conv<PhoneNumber> { it.value }
            else -> null
        }
}

private typealias Conv<T> = Converter<T, String>

private class LocalDateConverter : TypeAdapter<LocalDate>() {
    private val formatter = ISO_LOCAL_DATE

    override fun write(output: JsonWriter, value: LocalDate) {
        output.value(value.format(formatter))
    }

    override fun read(input: JsonReader): LocalDate {
        return parseBest(input.nextString())
    }
}

private class LocalTimeConverter : TypeAdapter<LocalTime>() {
    private val formatter = ISO_LOCAL_TIME

    override fun write(output: JsonWriter, value: LocalTime) {
        val adjustedVale = value.truncatedTo(ChronoUnit.MILLIS)
        output.value(adjustedVale.format(formatter))
    }

    override fun read(input: JsonReader): LocalTime {
        return LocalTime.parse(input.nextString(), formatter)
    }
}

private class LocalDateTimeConverter : TypeAdapter<LocalDateTime>() {
    private val formatter = ISO_LOCAL_DATE_TIME

    override fun write(output: JsonWriter, value: LocalDateTime) {
        val adjustedVale = value.truncatedTo(ChronoUnit.MILLIS)
        output.value(adjustedVale.format(formatter))
    }

    override fun read(input: JsonReader): LocalDateTime {
        return parseBest(input.nextString())
    }
}

private class ZonedDateTimeConverter : TypeAdapter<ZonedDateTime>() {
    private val formatter = ISO_ZONED_DATE_TIME

    override fun write(output: JsonWriter, value: ZonedDateTime) {
        val adjustedVale = value.truncatedTo(ChronoUnit.MILLIS)
        output.value(adjustedVale.format(formatter))
    }

    override fun read(input: JsonReader): ZonedDateTime {
        return parseBest(input.nextString())
    }
}

private class DurationConverter : TypeAdapter<Duration>() {
    override fun write(output: JsonWriter, value: Duration) {
        output.value(value.toMillis())
    }

    override fun read(input: JsonReader): Duration {
        return Duration.ofMillis(input.nextLong())
    }
}

private class CurrencyAmountConverter : TypeAdapter<CurrencyAmount>() {
    override fun write(output: JsonWriter, value: CurrencyAmount) {
        output.value(value.amount)
    }

    override fun read(input: JsonReader): CurrencyAmount {
        return CurrencyAmount(input.nextString())
    }
}

private class EmailAddressConverter : TypeAdapter<EmailAddress>() {
    override fun write(output: JsonWriter, value: EmailAddress) {
        output.value(value.value)
    }

    override fun read(input: JsonReader): EmailAddress {
        return EmailAddress(input.nextString())
    }
}

private class PhoneNumberConverter : TypeAdapter<PhoneNumber>() {
    override fun write(output: JsonWriter, value: PhoneNumber) {
        output.value(value.value)
    }

    override fun read(input: JsonReader): PhoneNumber {
        return PhoneNumber(input.nextString())
    }
}

private val relaxedDateTimeParser = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .optionalStart()
    .appendLiteral('T')
    .append(ISO_LOCAL_TIME)
    .optionalStart()
    .optionalStart()
    .appendOffset("+HHMM", "Z")
    .optionalEnd()
    .optionalStart()
    .appendOffset("+HH:MM", "Z")
    .optionalEnd()
    .optionalStart()
    .appendZoneId()
    .optionalEnd()
    .optionalEnd()
    .optionalEnd()
    .toFormatter()
    .withLocale(Locale.US)
    .withResolverStyle(ResolverStyle.STRICT)
    .withChronology(IsoChronology.INSTANCE)

private val defaultZoneId = ZoneId.of("Z")

private inline fun <reified T : Temporal> parseBest(value: String): T {
    val bestMatch = relaxedDateTimeParser.parseBest(
        value,
        ZonedDateTime.FROM,
        LocalDateTime.FROM,
        LocalDate.FROM
    )

    return when (T::class) {
        ZonedDateTime::class -> when (bestMatch) {
            is ZonedDateTime -> bestMatch
            is LocalDateTime -> bestMatch.atZone(defaultZoneId)
            is LocalDate -> bestMatch.atStartOfDay(defaultZoneId)
            else -> null
        }
        LocalDateTime::class -> when (bestMatch) {
            is LocalDateTime -> bestMatch
            is ZonedDateTime -> bestMatch.toLocalDateTime()
            is LocalDate -> bestMatch.atStartOfDay()
            else -> null
        }
        LocalDate::class -> when (bestMatch) {
            is LocalDate -> bestMatch
            is ZonedDateTime -> bestMatch.toLocalDate()
            is LocalDateTime -> bestMatch.toLocalDate()
            else -> null
        }
        else -> null
    } as? T ?: throw DateTimeException("Can't transform $bestMatch into a ${T::class}")
}
