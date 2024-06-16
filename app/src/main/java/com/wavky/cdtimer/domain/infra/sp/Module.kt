package com.wavky.cdtimer.domain.infra.sp

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val spModule = module {
  single { AdUrlSp(androidContext()) }
}
