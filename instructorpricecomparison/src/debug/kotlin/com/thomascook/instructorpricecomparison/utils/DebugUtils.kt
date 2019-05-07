package com.thomascook.instructorpricecomparison.utils

import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

/**
 * Collection of functions for debug builds
 */
private const val TAG = "DebugUtils"

object DebugUtils {
    fun hookNetworkInterceptor(client: OkHttpClient.Builder) {
        client.addNetworkInterceptor(StethoInterceptor())
        Log.i(TAG, "Hooked up Stetho interceptor")
    }
}