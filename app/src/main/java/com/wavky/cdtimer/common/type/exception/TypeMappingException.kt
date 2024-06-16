package com.wavky.cdtimer.common.type.exception

import com.wavky.cdtimer.common.type.exception.base.CodingErrorException

/**
 * Exception for casting one type to another.
 */
class TypeMappingException(
  sourceType: Any?,
  targetType: Class<out Any>,
  causeOf: Throwable? = null,
) :
  CodingErrorException(
    "Source class [${sourceType?.javaClass?.simpleName ?: ""}] can not map to" +
      " target class [${targetType.simpleName}].",
    causeOf
  )
