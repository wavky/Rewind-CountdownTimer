package com.wavky.cdtimer.common.ext

import java.math.BigDecimal

fun String?.ifNullOrEmpty(default: String): String {
  return if (this.isNullOrEmpty()) default else this
}

fun String?.toBigDecimalOrZero(): BigDecimal {
  return if (this.isNullOrEmpty()) {
    BigDecimal.ZERO
  } else {
    toBigDecimalOrNull() ?: BigDecimal.ZERO
  }
}
