package com.wavky.cdtimer.common.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.wavky.cdtimer.common.type.SignalEvent
import com.wavky.cdtimer.common.type.SingleLiveEvent
import com.wavky.cdtimer.common.type.Status
import com.wavky.cdtimer.common.type.StatusEvent

fun <T, O> MediatorLiveData<T>.concate(
  other: LiveData<O>,
  converter: (O) -> T,
): MediatorLiveData<T> {
  addSource(other) {
    value = converter(it)
  }
  return this
}

fun <T> SingleLiveEvent<T>.send(event: T) {
  this.value = event
}

fun SignalEvent.send() {
  this.value = Unit
}

@Suppress("NOTHING_TO_INLINE")
inline fun StatusEvent.send(cause: Throwable) = sendFailure(cause)

fun StatusEvent.sendFailure(cause: Throwable) {
  this.value = Status.failureOf(cause)
}

fun StatusEvent.sendSuccess() {
  this.value = Status.success
}

fun <T> StatusEvent.catch(block: () -> T) {
  try {
    block.invoke()
  } catch (e: Throwable) {
    send(e)
  }
}

suspend fun <T> StatusEvent.catch(block: suspend () -> T) {
  try {
    block.invoke()
  } catch (e: Throwable) {
    send(e)
  }
}
