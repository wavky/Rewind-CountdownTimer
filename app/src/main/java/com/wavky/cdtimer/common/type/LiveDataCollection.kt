package com.wavky.cdtimer.common.type

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

class LiveDataCollection(vararg liveData: LiveData<*>) {

  private val dataList = mutableListOf<LiveData<*>>()

  init {
    dataList.addAll(liveData)
  }

  fun add(liveData: LiveData<*>) {
    dataList.add(liveData)
  }

  fun clear() {
    dataList.clear()
  }

  fun removeObserversWith(owner: LifecycleOwner) {
    dataList.forEach { liveData: LiveData<*> ->
      liveData.removeObservers(owner)
    }
  }
}
