package com.thomascook.instructorpricecomparison.application

import android.content.Context
import android.os.StrictMode
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import androidx.appcompat.app.AppCompatActivity
import com.thomascook.core.CoreUtils
import kotlin.reflect.KClass

private const val TAG = "DebugApplication"

private fun enableStrictMode() {
    //Add support for strict mode in debug code
    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build())
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .build())
}

private fun enableStetho(context: Context) {
    Stetho.initialize(
        Stetho.newInitializerBuilder(context)
            .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
            .build())
}

/**
 * Application class used in DEBUG builds only
 */
class DebugApplication : androidx.multidex.MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        //Set up injection provider
        Injector.provider = DebugAndroidInjectionProvider(this)

        //Set up stetho
        enableStetho(this)

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        //Enable strict mode
        enableStrictMode()

        //Install memory leak detection
        LeakCanary.install(this)
    }
}

/**
 *  Activity used for instrumentation tests
 */
class DebugEmptyActivity : AppCompatActivity(), CoreUtils.ListenerProvider {
    var listener: CoreUtils.ListenerProvider? = null

    override fun <T : Any> getListenerForType(forType: KClass<T>): T? =
        listener?.getListenerForType(forType)
}