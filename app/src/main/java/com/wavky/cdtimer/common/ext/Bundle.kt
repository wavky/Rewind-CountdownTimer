package com.wavky.cdtimer.common.ext

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import java.io.Serializable

@Suppress("DEPRECATION")
fun <T> Bundle.getParcelableCompact(key: String?, clazz: Class<T>): T? =
  if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
    getParcelable(key, clazz)
  } else {
    getParcelable(key) as? T
  }

@Suppress("UNCHECKED_CAST", "DEPRECATION")
fun <T : Serializable> Bundle.getSerializableCompact(key: String?, clazz: Class<T>): T? =
  if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
    getSerializable(key, clazz)
  } else {
    getSerializable(key) as? T
  }
