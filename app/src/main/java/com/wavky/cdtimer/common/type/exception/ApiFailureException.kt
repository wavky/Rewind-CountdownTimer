package com.wavky.cdtimer.common.type.exception

import com.wavky.cdtimer.common.type.exception.base.ApiException

class ApiFailureException(
  val statusCode: Int,
  val msg: String?,
  val body: String? = null,
  cause: Throwable? = null,
) : ApiException("Status: $statusCode, Message: $msg, Body: $body", cause)
