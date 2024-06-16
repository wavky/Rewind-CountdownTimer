package com.wavky.cdtimer.common.util

import android.content.Context
import android.content.Intent
import android.os.BadParcelableException
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.wavky.cdtimer.BuildConfig

object Debug {
  val isEnable = BuildConfig.DEBUG

  /**
   * デフォルトのタグを使用してメッセージを表示します。<br></br>
   * デフォルトで呼び出し元のクラス名がタグとして使用されます。
   */
  fun log(message: String?) {
    if (isEnable && !TextUtils.isEmpty(message)) {
      log(shortClassName, message)
    }
  }

  /**
   * ログを表示します。
   *
   * @param message メッセージ
   */
  fun log(tag: String?, message: String?) {
    if (isEnable && !TextUtils.isEmpty(message)) {
      Log.d(tag, message!!)
    }
  }

  /**
   * ログを表示します。
   *
   * @param message メッセージ
   * @param tr [Throwable]
   */
  fun log(message: String?, tr: Throwable?) {
    if (isEnable) {
      Log.d(shortClassName, message, tr)
    }
  }

  /**
   * ログを表示します。
   *
   * @param message メッセージ
   * @param tr [Throwable]
   */
  fun log(tag: String?, message: String?, tr: Throwable?) {
    if (isEnable) {
      Log.d(tag, message, tr)
    }
  }

  /**
   * verboseログを表示します。
   *
   * @param message メッセージ
   */
  fun v(message: String?) {
    if (isEnable) {
      Log.v(shortClassName, message!!)
    }
  }

  /**
   * infoログを表示します。
   *
   * @param tag Log Tag
   * @param message メッセージ
   */
  fun i(tag: String?, message: String?) {
    Log.i(tag, message!!)
  }

  /**
   * ワーニングレベルログを表示します。
   *
   * @param message メッセージ
   */
  fun w(message: String?) {
    Log.w(shortClassName, message!!)
  }

  /**
   * ワーニングレベルログを表示します。
   *
   * @param tag ログタグ
   * @param message メッセージ
   */
  fun w(tag: String?, message: String?) {
    Log.w(tag, message!!)
  }

  /**
   * Throwable付きのワーニングレベルログを表示します。
   *
   * @param message メッセージ
   * @param tr Throwable
   */
  fun w(message: String?, tr: Throwable?) {
    w(shortClassName, message, tr)
  }

  /**
   * Throwable付きのワーニングレベルログを表示します。
   *
   * @param tag ログタグ
   * @param message メッセージ
   * @param tr Throwable
   */
  fun w(tag: String?, message: String?, tr: Throwable?) {
    Log.w(tag, message, tr)
  }

  /**
   * エラーログを表示します。
   *
   * @param message メッセージ
   */
  fun e(tag: String?, message: String?) {
    Log.e(tag, message!!)
  }

  /**
   * Throwable付きのエラーレベルログを表示します。
   *
   * @param message メッセージ
   * @param tr Throwable
   */
  fun e(message: String?, tr: Throwable?) {
    e(shortClassName, message, tr)
  }

  /**
   * Throwable付きのエラーレベルログを表示します。
   *
   * @param tag ログタグ
   * @param message メッセージ
   * @param tr Throwable
   */
  fun e(tag: String?, message: String?, tr: Throwable?) {
    Log.e(tag, message, tr)
  }

  private val shortClassName: String
    /**
     * 短いクラス名を返します
     *
     * @return クラス名
     */
    get() {
      var shortName = "unknown"
      if (isEnable) {
        val stackTraceElement = Throwable().stackTrace[2]
        if (stackTraceElement != null) {
          val fullName = stackTraceElement.className
          shortName = fullName.substring(fullName.lastIndexOf(".") + 1)
        }
      }
      return shortName
    }

  /**
   * ショートバージョンのトーストを表示します。
   *
   * @param context [Context]
   * @param message メッセージ
   */
  fun showShortToast(context: Context?, message: String?) {
    if (isEnable && !TextUtils.isEmpty(message)) {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * ロングバージョンのトーストを表示します。
   *
   * @param context [Context]
   * @param message メッセージ
   */
  fun showLongToast(context: Context?, message: String?) {
    if (isEnable && !TextUtils.isEmpty(message)) {
      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
  }

  /**
   * [Intent]の中身をログ出力します。
   */
  fun intentLogger(intent: Intent?) {
    if (isEnable) {
      val tag = shortClassName
      if (intent == null) {
        Log.v(tag, "Intent is null")
        return
      }
      val component = intent.component
      Log.v(
        tag, """
   Intent[@${Integer.toHexString(intent.hashCode())}]
   Action: ${intent.action}
   Category: ${intent.categories}
   Data: ${intent.dataString}
   Component: ${if (component != null) component.packageName + "/" + component.className else "null"}${
          String.format(
            "%nFlags: 0x%08x",
            intent.flags
          )
        }
   """.trimIndent()
      )
      dumpExtra(tag, intent)
    }
  }

  private fun dumpExtra(tag: String, intent: Intent) {
    val extra = intent.extras
    if (extra == null || extra.isEmpty) {
      Log.v(tag, "has no extra")
      return
    }
    val keySet = extra.keySet()
    for (key in keySet) {
      try {
        Log.v(tag, "Extra[" + key + "]: " + extra[key].toString())
      } catch (e: BadParcelableException) {
        Log.w(tag, "key [$key] is something wrong.\n$intent", e)
      }
    }
  }
}
