package com.centurylink.biwf.model.primitive

import java.math.BigDecimal
import java.util.Currency

/**
 * Represents a currency of a given [amount] in a given [currency].
 */
data class CurrencyAmount(val amount: BigDecimal, val currency: Currency) {

    companion object {
        private const val DEFAULT_CURRENCY = "USD"
        private const val ROUNDING_MODE: Int = BigDecimal.ROUND_HALF_UP

        /**
         * Creates a [CurrencyAmount] from a [Number] and a [currency].
         *
         * If the [Number] is not a [BigDecimal], it will be forced into a [BigDecimal] with
         * a [BigDecimal.scale] of 2 (2 digits after decimal point).
         */
        operator fun invoke(
            amount: Number = 0,
            currency: String = DEFAULT_CURRENCY
        ): CurrencyAmount = when (amount) {
            is Int -> BigDecimal(amount).adjust
            is Long -> BigDecimal(amount).adjust
            is BigDecimal -> amount
            else -> BigDecimal(amount.toDouble()).adjust
        }.let { CurrencyAmount(it, Currency.getInstance(currency)) }

        /**
         * Creates a [CurrencyAmount] from a [String] and a [currency].
         *
         * The [CurrencyAmount.amount] will be forced into a [BigDecimal] with
         * a [BigDecimal.scale] of 2 (2 digits after decimal point).
         */
        operator fun invoke(
            amount: String, currency:
            String = DEFAULT_CURRENCY
        ): CurrencyAmount =
            CurrencyAmount(BigDecimal(amount).adjust, Currency.getInstance(currency))

        private inline val BigDecimal.adjust get() = setScale(2, ROUNDING_MODE)
    }

    override fun toString(): String = "$amount${currency.currencyCode}"
}
