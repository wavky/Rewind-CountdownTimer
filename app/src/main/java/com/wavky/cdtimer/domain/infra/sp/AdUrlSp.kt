package com.wavky.cdtimer.domain.infra.sp

import android.content.Context
import com.wavky.cdtimer.common.type.BaseSp

class AdUrlSp(context: Context) : BaseSp(context) {
  override val fileName: String = "adUrls"

  private val key = "adUrls"

  var adUrls: List<String>
    get() = sp.getStringSet(key, setOf())?.toList() ?: emptyList()
    set(value) = sp.edit().putStringSet(key, value.toSet()).apply()
}
