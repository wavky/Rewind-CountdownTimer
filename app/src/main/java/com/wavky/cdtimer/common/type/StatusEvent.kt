package com.wavky.cdtimer.common.type

typealias StatusEvent = SingleLiveEvent<Status>

sealed interface Status {
  data object Success : Status
  class Failure(val cause: Throwable) : Status

  companion object {
    val success = Success
    fun failureOf(cause: Throwable) = Failure(cause)
  }
}
