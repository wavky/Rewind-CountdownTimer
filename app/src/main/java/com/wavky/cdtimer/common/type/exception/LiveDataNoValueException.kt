package com.wavky.cdtimer.common.type.exception

import androidx.lifecycle.LiveData
import com.wavky.cdtimer.common.type.exception.base.CodingErrorException

/**
 * Exception for asserting one LiveData should has value but not.
 * Indicating this should be a logical mistake.
 */
class LiveDataNoValueException(instance: LiveData<*>) :
  CodingErrorException(
    "LiveData [${instance.javaClass.simpleName}] should has a value but not."
  )
