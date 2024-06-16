package com.wavky.cdtimer.common.util

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.math.min

/**
 * HttpLoggingInterceptorでログを出力すると、データ量の多いjsonが
 * 途中で切れてしまうのでデバッグ用に作成した。
 */
class Logger : HttpLoggingInterceptor.Logger {
  companion object {
    private const val MAX_LENGTH = 3000
    private const val LOG_TAG = "OkHttp"
  }

  override fun log(message: String) {
    val len = message.length
    var start = 0
    do {
      val end = min(len, start + MAX_LENGTH)
      val str = message.substring(start, end)
      Log.d(LOG_TAG, str)
      start = end
    } while (end < len)
  }
}
