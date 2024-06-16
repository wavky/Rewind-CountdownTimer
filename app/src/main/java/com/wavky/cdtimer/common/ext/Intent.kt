package com.wavky.cdtimer.common.ext

import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

inline fun <reified R> Intent.getParcelable(key: String): R? {
  return if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
    getParcelableExtra(key, R::class.java)
  } else {
    getParcelableExtra(key)
  }
}
