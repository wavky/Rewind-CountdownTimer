package com.wavky.cdtimer.common.type.exception.base

// checked exception
open class AppException : Exception {

  constructor(message: String, cause: Throwable? = null) : super(message, cause)

  constructor(cause: Throwable) : super(cause)
}
