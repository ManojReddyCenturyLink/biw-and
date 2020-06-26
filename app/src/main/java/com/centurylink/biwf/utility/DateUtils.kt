package com.centurylink.biwf.utility

import android.net.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        const val STANDARD_FORMAT = "yyyy-MM-dd"
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

        fun formatAppointmentTime(dateInput: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val d: Date? = inputFormat.parse(dateInput)
            val outputFormat = SimpleDateFormat("h:mmaa", Locale.US)
            return outputFormat.format(d)
        }

        fun formatAppointmentETA(startDate: String, endDate: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val startDate: Date? = inputFormat.parse(startDate)
            val endDate: Date? = inputFormat.parse(endDate)
            val outputFormat = SimpleDateFormat("haa", Locale.US)
            return "${outputFormat.format(startDate)}-${outputFormat.format(endDate)}"
        }

        fun toSimpleString(date: Date, format: String): String {
            val format = SimpleDateFormat(format, Locale.US)
            return format.format(date)
        }
    }
}