package com.thomascook.instructorpricecomparison.application

import android.annotation.SuppressLint
import android.content.Context
import com.thomascook.instructorpricecomparison.utils.DebugUtils
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Injection class for providing real instances of objects that are used in development
 */
private const val TIMEOUT_CONNECTION: Long = 20
private const val TIMEOUT_READ: Long = 20
private const val TAG = "InjectionDev"

class ReleaseInjection internal constructor(appContext: Context) : BaseInjectorProvider(appContext) {

    // Create instance of OK http client so it's reused.
    private val okHttpClient by lazy {
        val httpBuilder = OkHttpClient.Builder()
        httpBuilder.connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
        httpBuilder.readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)

        // Hook up for Stetho interceptions
        DebugUtils.hookNetworkInterceptor(httpBuilder)

        try {
            val trustManager = object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            }

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            httpBuilder.sslSocketFactory(sslSocketFactory, trustManager)
            httpBuilder.hostnameVerifier { _, _ -> true }

        } catch (error: Exception) {
            EventReporter.e(TAG, "Failed to create all trust manager", error)
        }
        httpBuilder.build()
    }

    //Provides OK http client
    override fun provideOkHttpClient(): OkHttpClient = okHttpClient
}