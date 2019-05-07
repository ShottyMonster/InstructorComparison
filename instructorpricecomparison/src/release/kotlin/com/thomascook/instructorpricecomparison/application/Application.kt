package com.thomascook.instructorpricecomparison.application

import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.thomascook.instructorpricecomparison.R

/**
 * Default application class for the application
 */
class Application : android.app.Application() {
    override fun onCreate() {
        super.onCreate()

        // Set up injection provider
        Injector.provider = ReleaseInjection(this)

        // Set up app center
        // We want to use Crashes API for reporting crashes to the cloud
        // We want to us Analytics API for reporting none hard crash info to the cloud
        AppCenter.start(this, getString(R.string.app_center_api_key),
            Analytics::class.java, Crashes::class.java)
    }
}