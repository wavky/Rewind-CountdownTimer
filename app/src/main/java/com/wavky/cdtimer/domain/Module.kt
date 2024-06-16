package com.wavky.cdtimer.domain

import com.wavky.cdtimer.domain.infra.api.apiModule
import com.wavky.cdtimer.domain.infra.sp.spModule
import com.wavky.cdtimer.domain.repository.repositoryModule
import com.wavky.cdtimer.domain.service.serviceModule

val domainModules =
  listOf(apiModule, spModule, repositoryModule, serviceModule)
