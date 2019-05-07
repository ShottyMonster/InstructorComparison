package com.thomascook.instructorpricecomparison.utils

import okhttp3.OkHttpClient

/**
 * Collection of functions for debug builds
 */
private const val TAG = "DebugUtils"

object DebugUtils {
    fun hookNetworkInterceptor(@Suppress("UNUSED_PARAMETER") builder: OkHttpClient.Builder) {
    }
}