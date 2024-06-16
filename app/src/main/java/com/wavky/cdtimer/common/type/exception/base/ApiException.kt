package com.wavky.cdtimer.common.type.exception.base

// checked exception
open class ApiException : AppException {

  constructor(message: String, cause: Throwable?) : super(message, cause)

  constructor(cause: Throwable) : super(cause)
}
