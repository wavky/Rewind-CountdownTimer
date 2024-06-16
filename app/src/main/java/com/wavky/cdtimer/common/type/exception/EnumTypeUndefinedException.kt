package com.wavky.cdtimer.common.type.exception

import com.wavky.cdtimer.common.type.exception.base.CodingErrorException

class EnumTypeUndefinedException(
  source: Any?,
  targetEnumType: Class<out Any>,
) :
  CodingErrorException(
    "The instance of [$source] is not defined in the enumeration type ${targetEnumType.simpleName}."
  )
