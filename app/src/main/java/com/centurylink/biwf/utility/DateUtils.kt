package com.centurylink.biwf.utility

import android.net.ParseException
import org.threeten.bp.LocalDateTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        const val STANDARD_FORMAT = "yyyy-MM-dd"
        const val CANCEL_APPOINTMENT_DATE_FORMAT = "MMMM dd, yyyy"
        fun formatInvoiceDate(dateInput: String): String {
            var formattedDate: String = ""
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
            val output = SimpleDateFormat("MM/dd/yy", Locale.US)
            var d: Date? = null
            try {
                d = input.parse(dateInput)
                formattedDate = output.format(d)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return formattedDate
        }

        fun formatAppointmentDate(dateInput: String): String {
            if (dateInput.equals(LocalDateTime.MAX.toString())) {
                return ""
            }
            var format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d: Date? = format.parse(dateInput)
            val date = format.format(d)
            format =
                if (date.endsWith("1") && !date.endsWith("11")) SimpleDateFormat(
                    "MMM. d'st' yyyy",
                    Locale.US
                )
                else if (date.endsWith("2") && !date.endsWith("12")) SimpleDateFormat(
                    "MMM. d'nd' yyyy",
                    Locale.US
                )
                else if (date.endsWith("3") && !date.endsWith("13")) SimpleDateFormat(
                    "MMM. d'rd' yyyy",
                    Locale.US
                )
                else SimpleDateFormat("MMM. d'th' yyyy", Locale.US)

            return format.format(d)
        }

        fun toSimpleString(date: Date, format: String): String {
            val format = SimpleDateFormat(format, Locale.US)
            return format.format(date)
        }

        fun fromStringtoDate(date: String): Date {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return inputFormat.parse(date)
        }

        fun getFirstDateofthisMonth(): Date {
            val c = Calendar.getInstance() // this takes current date
            c.set(Calendar.DAY_OF_MONTH, 1)
            return c.time
        }

        fun getLastDateoftheMonthAfter(): Date {
            val c = Calendar.getInstance()
            c.add(Calendar.DATE, 60)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            return c.time
        }

        fun addDays(date: Date?, days: Int): Date? {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, days) //minus number would decrement the days
            return cal.time
        }

        fun formatAppointmentBookedDate(dateInput: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("MM/dd/yy", Locale.US)
            var formattedDate = ""
            var d: Date? = null
            try {
                d = inputFormat.parse(dateInput)
                formattedDate = outputFormat.format(d)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return formattedDate
        }

        fun formatAppointmentTimeValuesWithTimeZone(dateInput: String, timezone: String): String {
            if (dateInput.equals(LocalDateTime.MAX.toString())) {
                return ""
            }
            val gmtDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            gmtDateFormat.timeZone = TimeZone.getTimeZone("GMT")
            val returnTypeDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            returnTypeDateFormat.timeZone = TimeZone.getTimeZone(timezone)
            val formattedDate = returnTypeDateFormat.format(gmtDateFormat.parse(dateInput))
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val d: Date? = inputFormat.parse(formattedDate)
            val outputFormat = SimpleDateFormat("h:mmaa", Locale.US)
            return outputFormat.format(d)
        }
    }
}