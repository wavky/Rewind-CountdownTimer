package com.wavky.cdtimer.common.ext

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job

fun LifecycleOwner.throttleClicksOn(view: View, onClick: suspend () -> Unit): Job =
  view.throttleClicksIn(lifecycleScope, onClick)

inline fun <reified T> LifecycleOwner.observe(
  live: LiveData<T>,
  crossinline observer: (T) -> Unit,
) {
  live.observe(this) { observer(it) }
}
