package com.centurylink.biwf.utility

import android.net.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        fun formatPaymentProcessedDate(dateInput: String): String {
            var formattedDate: String = ""
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val output = SimpleDateFormat("dd/MM/yyyy")
            var d: Date? = null
            try {
                d = input.parse(dateInput)
                formattedDate = output.format(d)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return formattedDate
        }
    }
}