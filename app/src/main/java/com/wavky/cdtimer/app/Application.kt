package com.wavky.cdtimer.app

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class Application : android.app.Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger()
      androidContext(this@Application)
      modules(appModules)
    }
  }
}
