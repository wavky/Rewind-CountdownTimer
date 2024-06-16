package com.wavky.cdtimer.common.type.exception.base

/**
 * unexpected, un-checked exception
 *
 * Represent an exception causes by programming error, human miss.
 */
open class CodingErrorException(message: String, cause: Throwable? = null) :
  RuntimeException(message, cause)
