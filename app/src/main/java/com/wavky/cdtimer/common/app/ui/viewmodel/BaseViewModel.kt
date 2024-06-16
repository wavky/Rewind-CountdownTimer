package com.wavky.cdtimer.common.app.ui.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.wavky.cdtimer.common.type.LiveDataCollection

abstract class BaseViewModel : ViewModel() {

  protected abstract val liveDataCollection: LiveDataCollection

  fun removeObserversWith(owner: LifecycleOwner) =
    liveDataCollection.removeObserversWith(owner)
}

fun <T : BaseViewModel> T.resetObserversWith(owner: LifecycleOwner, block: T.() -> Unit): T {
  removeObserversWith(owner)
  block()
  return this
}
