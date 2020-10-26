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
        if (digits.length == 11) {
            if (digits.first() == '1') {
                return digits.drop(1)
            }
        }
        return digits
    }

    override fun toString(): String {
        if (areaCode.isNullOrEmpty() || exchangeCode.isNullOrEmpty()) {
            return ""
        }
        return "($areaCode) $exchangeCode-$subscriberNumber"
    }
}
