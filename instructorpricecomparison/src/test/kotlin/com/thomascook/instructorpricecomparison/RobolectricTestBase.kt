package com.thomascook.instructorpricecomparison

import android.app.Application
import android.content.Context
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/**
 * Base class for test that use robolectric
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24],
        application = RobolectricTestBase.ApplicationStub::class,
        manifest = "AndroidManifest.xml")
abstract class RobolectricTestBase {
    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    fun cacheDir(): File = context.cacheDir

    val context: Context
        get() = RuntimeEnvironment.application

    internal class ApplicationStub : Application()
}

/**
 * The idea was shamelessly taken from here:
 * https://hk.saowen.com/a/f2c3d66769a8033dec9a233b77fb127b4e35f700d94321b16bd642a344f06932
 */
class RxImmediateSchedulerRule : TestRule {

    override fun apply(base: Statement, d: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler {
                    Schedulers.trampoline()
                }
                RxJavaPlugins.setComputationSchedulerHandler {
                    Schedulers.trampoline()
                }
                RxJavaPlugins.setNewThreadSchedulerHandler {
                    Schedulers.trampoline()
                }
                RxAndroidPlugins.setInitMainThreadSchedulerHandler {
                    Schedulers.trampoline()
                }

                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                    RxAndroidPlugins.reset()
                }
            }
        }
    }
}
