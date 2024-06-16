package com.wavky.cdtimer.common.ext

fun <T> T.toSingleItemList(): List<T> {
  return listOf(this)
}
