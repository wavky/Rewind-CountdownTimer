package com.wavky.cdtimer.common.ext

import android.widget.TextView
import com.wavky.cdtimer.common.type.Text
import com.wavky.cdtimer.common.type.Text.CharSequenceText
import com.wavky.cdtimer.common.type.Text.ResourceText

fun TextView.setText(value: Text?) {
  text = when (value) {
    is CharSequenceText -> value.text
    is ResourceText -> context.getString(value.resId)
    null -> null
  }
}

var TextView.tText: Text?
  get() = CharSequenceText(text)
  set(value) {
    setText(value)
  }
