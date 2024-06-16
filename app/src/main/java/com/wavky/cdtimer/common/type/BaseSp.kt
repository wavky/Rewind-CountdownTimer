package com.wavky.cdtimer.common.type

import android.content.Context
import android.content.SharedPreferences

abstract class BaseSp(context: Context) {

  abstract val fileName: String

  val sp: SharedPreferences by lazy {
    context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
  }

  fun clear(): Boolean = sp.edit().clear().commit()
}
