package com.wavky.cdtimer.domain.infra.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wavky.cdtimer.BuildConfig
import com.wavky.cdtimer.common.const.Config
import com.wavky.cdtimer.common.util.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val apiModule = module {
  fun gson(): Gson = GsonBuilder().create()

  fun okHttpClient(): OkHttpClient =
    OkHttpClient()
      .newBuilder()
      .apply {
        connectTimeout(Config.API_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        // 401認証エラーハンドリング
//        authenticator(JwtAuthenticator())
        // log interceptor
        val interceptor = HttpLoggingInterceptor(Logger())
        interceptor.level = if (BuildConfig.DEBUG) {
          HttpLoggingInterceptor.Level.BODY
        } else {
          HttpLoggingInterceptor.Level.BASIC
        }
        addInterceptor(interceptor)
      }
      // jwt 認証header interceptor
//      .addInterceptor(HeaderInterceptor())
      .callTimeout(Config.API_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
      .build()

  val domain = Config.DOMAIN

  fun retrofit(client: OkHttpClient, gson: Gson): Retrofit =
    Retrofit.Builder()
      .baseUrl("https://$domain/")
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()

  single { gson() }
  single { okHttpClient() }
  single { retrofit(get(), get()) }
}
