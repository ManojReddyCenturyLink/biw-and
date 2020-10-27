package com.centurylink.biwf.utility

import java.util.regex.Matcher
import java.util.regex.Pattern

class NumberUtil {

    companion object {

        fun getOnlyDigits(s: String?): String? {
            val pattern: Pattern = Pattern.compile("[^0-9]")
            val matcher: Matcher = pattern.matcher(s)
            return matcher.replaceAll("")
        }
    }
}
