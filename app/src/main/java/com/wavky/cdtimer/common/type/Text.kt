package com.wavky.cdtimer.common.type

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Text : Parcelable {
  data class CharSequenceText(val text: CharSequence) : Text()
  data class ResourceText(@StringRes val resId: Int) : Text()

  companion object {
    fun of(text: CharSequence) = CharSequenceText(text)
    fun of(@StringRes resId: Int) = ResourceText(resId)
  }

  fun toCharSequence(context: Context?): CharSequence {
    return when (this) {
      is CharSequenceText -> text
      is ResourceText -> {
        requireNotNull(context)
        context.getString(resId)
      }
    }
  }
}
