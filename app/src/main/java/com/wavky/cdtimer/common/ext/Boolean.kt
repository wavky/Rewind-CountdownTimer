package com.wavky.cdtimer.common.ext

fun Boolean.toInt() = if (this) 1 else 0
inline fun Boolean.doOnSuccess(action: () -> Unit) {
  if (this) action()
}

inline fun Boolean.doOnFailure(action: () -> Unit) {
  if (!this) action()
}
