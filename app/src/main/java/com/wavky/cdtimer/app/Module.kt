package com.wavky.cdtimer.app

import com.wavky.cdtimer.app.main.MainViewModel
import com.wavky.cdtimer.domain.domainModules
import com.wavky.cdtimer.usecase.useCaseModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModules
  get() = listOf(appModule, useCaseModule) + domainModules

val appModule = module {
  viewModel { MainViewModel() }
}
