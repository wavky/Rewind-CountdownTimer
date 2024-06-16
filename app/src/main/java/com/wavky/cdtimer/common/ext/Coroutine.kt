package com.wavky.cdtimer.common.ext

import android.view.View
import com.wavky.cdtimer.common.type.StatusEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> handleCoroutineException(onException: (e: Throwable) -> T): CoroutineExceptionHandler =
  CoroutineExceptionHandler { _, throwable -> onException(throwable) }

fun handleCoroutineException(
  receiver: StatusEvent,
  onException: (() -> Unit)? = null,
): CoroutineExceptionHandler =
  CoroutineExceptionHandler { _, throwable ->
    receiver.send(throwable)
    onException?.invoke()
  }

fun ignoreCoroutineException(): CoroutineExceptionHandler =
  CoroutineExceptionHandler { _, _ -> }

suspend fun delayForRippleDone() {
  delay(200)
}

/**
 * Only emit the first element within a specified time window, and emit the next element after the window period expires.
 * @param windowDuration Long
 */
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> =
  flow {
    var lastEmissionTime = 0L
    collect { upstream ->
      val currentTime = System.currentTimeMillis()
      val mayEmit = currentTime - lastEmissionTime > windowDuration
      if (mayEmit) {
        lastEmissionTime = currentTime
        emit(upstream)
      }
    }
  }

/**
 * Execute a click only once within a specified time frame to prevent duplicate clicks.
 * @param view Button
 * @param onEach Job to execute on each click
 */
fun CoroutineScope.throttleClicksOn(view: View, onEach: suspend () -> Unit): Job =
  view.throttleClicksIn(this, onEach)
