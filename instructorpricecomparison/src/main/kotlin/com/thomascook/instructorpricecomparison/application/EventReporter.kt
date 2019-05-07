package com.thomascook.instructorpricecomparison.application

import android.util.Log
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ"
private const val ANALYTICS_TYPE_ERROR = "error"
private const val ARG_TAG = "tag"
private const val ARG_CAUSE = "cause"
private const val ARG_MESSAGE = "message"
private const val ARG_STACK_TRACE = "stack trace"

object EventReporter {
    var console = false
    private val formatter = SimpleDateFormat(DATE_FORMAT, Locale.US)

    fun e(tag: String, message: String, e: Throwable? = null) {
        if (console) {
            System.err.println(formatter.format(Date()) + "\t" + tag + "\t" + message)
            e?.let {
                e.printStackTrace()
            }
        } else {
            e?.let { throwable ->
                if (AppCenter.isConfigured()) {
                    Analytics.trackEvent(ANALYTICS_TYPE_ERROR, createErrorMap(throwable, tag))
                } else {
                    Log.e(tag, message, throwable)
                }
            }
        }
    }

    fun d(tag: String, message: String, e: Throwable? = null) {
        if (console) {
            System.out.println(formatter.format(Date()) + "\t" + tag + "\t" + message)
            e?.let {
                e.printStackTrace()
            }
        } else {
            Log.d(tag, message, e)
        }
    }

    fun i(tag: String, message: String) {
        if (console) {
            System.out.println(formatter.format(Date()) + "\t" + tag + "\t" + message)
        } else {
            Log.i(tag, message)
        }
    }

    private fun createErrorMap(error: Throwable, componentTag: String) = mapOf(
        Pair(ARG_TAG, componentTag),
        Pair(ARG_CAUSE, error.cause.toString()),
        Pair(ARG_MESSAGE, error.message.toString()),
        Pair(ARG_STACK_TRACE, error.stackTrace.toString())
    )
}