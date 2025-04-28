package com.example.bitcoinwallet.common

import java.math.BigDecimal
import java.math.RoundingMode

fun Long.toBtcString(): String {
    return BigDecimal(this).divide(
        BigDecimal(100_000_000), 8, RoundingMode.DOWN
    ).stripTrailingZeros().toPlainString()
}

fun String.toSatoshiLong(): Long {
    return (this.toBigDecimal() * 100_000_000.toBigDecimal()).toLong()
}
