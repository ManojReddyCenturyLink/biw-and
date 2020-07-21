package com.centurylink.biwf.utility

data class PhoneNumber(val input: String) {
    val number: String by lazy { parseToTenDigitNumber() }
    val areaCode: String = number.take(3)
    private val exchangeCode = number.drop(3).take(3)
    private val subscriberNumber = number.takeLast(4)

    private fun parseToTenDigitNumber(): String {
        if (input.isNullOrEmpty()) {
            return ""
        }
        val digits = input.filter { it.isDigit() }

        if (digits.first() == '1') {
            require(digits.length == 11) { "Must be 11 digits including country code (1)" }
            return digits.drop(1)
        }

        require(digits.length == 10) { "Must be 10 digits excluding country code" }
        return digits
    }

    override fun toString(): String {
        if (areaCode.isNullOrEmpty() || exchangeCode.isNullOrEmpty()) {
            return ""
        }
        return "($areaCode) $exchangeCode-$subscriberNumber"
    }
}