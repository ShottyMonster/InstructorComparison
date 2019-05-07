package com.thomascook.instructorpricecomparison.application

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Injector provider for debug builds
 */
private const val TIMEOUT_CONNECTION: Long = 60
private const val TIMEOUT_READ: Long = 60
private const val TIMEOUT_WRITE: Long = 60

private fun provideOkHttpClient(): OkHttpClient {
    val httpBuilder = OkHttpClient.Builder()
    httpBuilder.connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
    httpBuilder.readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
    httpBuilder.writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
    httpBuilder.addNetworkInterceptor(StethoInterceptor())
    httpBuilder.followRedirects(false)
    httpBuilder.cookieJar(CookieStore.get())
    return httpBuilder.build()
}

private fun provideMoshi(): Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Injector provider used by the application in debug build configuration
 */
class DebugAndroidInjectionProvider(context: Context) : BaseInjectorProvider(context) {

}