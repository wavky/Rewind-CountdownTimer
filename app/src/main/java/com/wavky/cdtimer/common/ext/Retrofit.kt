package com.wavky.cdtimer.common.ext

import com.wavky.cdtimer.common.type.exception.ApiFailureException
import com.wavky.cdtimer.common.type.exception.base.AppException
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.resume

// For Retrofit >= v2.6 with coroutine support
/**
 * @return R 空レスポンスの場合、エラー扱いにする
 */
suspend fun <R> connectApiOnIo(apiBlock: suspend () -> Response<R>): R =
  withContext(IO) {
    apiBlock().run {
      if (isSuccessful) {
        this.body()
      } else {
        null
      } ?: throw ApiFailureException(code(), message(), errorBody()?.string())
    }
  }

/**
 * @return R? 空レスポンスの場合、nullを返す
 */
suspend fun <R> connectApiOnIoNullable(apiBlock: suspend () -> Response<R>): R? =
  withContext(IO) {
    apiBlock().run {
      if (isSuccessful) {
        this.body()
      } else {
        throw ApiFailureException(code(), message(), errorBody()?.string())
      }
    }
  }

//region For RxJava
suspend fun connectApiOnIoWithCompletable(apiBlock: () -> Completable) {
  withContext(IO) {
    val throwable = suspendCancellableCoroutine { coroutine ->
      apiBlock()
        .doOnError { e ->
          val wrappedThrowable =
            if (e is HttpException) {
              ApiFailureException(e.code(), e.message(), e.response()?.errorBody()?.string())
            } else {
              AppException(e)
            }
          coroutine.resume(wrappedThrowable)
        }
        .doOnComplete { coroutine.resume(null) }
        .subscribe()
    }
    if (throwable != null) throw throwable
  }
}

@Suppress("UNCHECKED_CAST")
suspend fun <T> connectApiOnIoWithObservable(apiBlock: () -> Observable<T>): T {
  return withContext(IO) {
    val result = suspendCancellableCoroutine { coroutine ->
      apiBlock()
        .doOnNext { coroutine.resume(it) }
        .doOnError { e ->
          val wrappedThrowable =
            if (e is HttpException) {
              ApiFailureException(e.code(), e.message(), e.response()?.errorBody()?.string())
            } else {
              AppException(e)
            }
          coroutine.resume(wrappedThrowable)
        }
        .subscribe()
    }
    if (result is Throwable) {
      throw result
    } else {
      return@withContext result as T
    }
  }
}
//endregion

@Suppress("BlockingMethodInNonBlockingContext")
// For Retrofit traditional caller invocation
suspend fun <R> Call<R>.executeOnIo(): R =
  withContext(IO) {
    execute().run {
      body() ?: throw ApiFailureException(code(), message(), errorBody()?.string())
    }
  }

