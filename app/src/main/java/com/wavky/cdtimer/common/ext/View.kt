package com.wavky.cdtimer.common.ext

import android.view.View
import com.wavky.cdtimer.common.const.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks

/**
 * Execute a (first) click only once within a specified time frame to prevent duplicate clicks.
 * @param scope CoroutineScope
 * @param onEach Job to execute on each click
 */
fun View.throttleClicksIn(scope: CoroutineScope, onEach: suspend () -> Unit): Job =
  this.clicks()
    .throttleFirst(Config.CLICK_EVENT_THROTTLE_FIRST_DURATION)
    .onEach {
      delayForRippleDone()
      onEach()
    }
    .launchIn(scope)

/**
 * Execute a (last) click only once within a specified time frame to prevent duplicate clicks.
 * @param scope CoroutineScope
 * @param onEach Job to execute on each click
 */
@FlowPreview
fun View.debounceItemClicksIn(scope: CoroutineScope, onEach: suspend () -> Unit): Job =
  this.clicks()
    .debounce(Config.CLICK_EVENT_DEBOUNCE_DURATION)
    .onEach {
      delayForRippleDone()
      onEach()
    }
    .launchIn(scope)
