package com.wavky.cdtimer.common.ext

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

fun <T> Fragment.observe(live: LiveData<T>, observer: (T) -> Unit) {
  live.observe(this.viewLifecycleOwner) { observer(it) }
}

fun <T> DialogFragment.observe(live: LiveData<T>, observer: (T) -> Unit) {
  live.observe(this) { observer(it) }
}

fun <T> Fragment.observe(vararg live: LiveData<T>, observer: (T) -> Unit) {
  live.forEach {
    observe(it, observer)
  }
}
