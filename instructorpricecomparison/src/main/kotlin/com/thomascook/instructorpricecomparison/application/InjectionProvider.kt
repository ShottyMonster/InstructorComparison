package com.thomascook.instructorpricecomparison.application

import android.content.Context
import com.thomascook.instructorpricecomparison.EncryptionDecryption
import com.thomascook.instructorpricecomparison.SecureKeyProvider
import com.thomascook.instructorpricecomparison.remote.MobileRemote
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.thomascook.instructorpricecomparison.application.permissions.AndroidPermissionsChecker
import com.thomascook.instructorpricecomparison.application.preferences.AndroidPreferences
import com.thomascook.instructorpricecomparison.application.remote.*
import com.thomascook.core.EncryptDecrypt
import com.thomascook.core.SecureKeyStore
import com.thomascook.instructorpricecomparison.application.storage.FirebaseStorage
import com.thomascook.instructorpricecomparison.permissions.PermissionsChecker
import com.thomascook.instructorpricecomparison.preferences.Preferences
import com.thomascook.instructorpricecomparison.storage.Storage
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import java.util.concurrent.atomic.AtomicReference
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Single class used to store reference to concrete implementation of injection provider
 */
object Injector {
    private val providerRef = AtomicReference<InjectorProvider>()

    @JvmStatic
    var provider: InjectorProvider
        get() = providerRef.get() ?: throw IllegalStateException("Injector has not been set")
        set(value) {
            providerRef.getAndSet(value)
        }
}

interface InjectorProvider {
    // Provide permissions checker
    fun providePermissionsChecker() : PermissionsChecker

    // Provide OkHttp client
    fun provideOkHttpClient() : OkHttpClient

    //Provide remote
    fun provideRemote() : MobileRemote

    // Provides preferences.
    fun providePreferences(): Observable<Preferences>

    // Provides encryption/decryption for the specified key
    fun provideEncryptionDecryption(key: ByteArray): EncryptionDecryption

    // Provide Moshi
    fun provideMoshi() : Moshi

    // Provide firebase store
    fun provideStorage() : Storage
}

private const val PREFS_AES_KEY_ALIAS = "key_com.thomascook.instructorpricecomparison_encryption"
private const val TAG = "BaseInjectorProvider"

abstract class BaseInjectorProvider(protected val appContext: Context) : InjectorProvider {
    // Key provider
    private val keyProvider by lazy {
        AndroidSecureKeyProvider(appContext)
    }

    // Reference to initialised preferences
    private val preferences by lazy {
        val keyAlias = "${appContext.applicationInfo.packageName ?: "key_thomascook"}.pref_key"

        // Get encryption key for preferences
        val prefKey = this.keyProvider.getKey(keyAlias)
        // Create application preferences
        AndroidPreferences(
            appContext,
            this.provideEncryptionDecryption(prefKey)
        )
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    override fun provideEncryptionDecryption(key: ByteArray) = AndroidEncryptionDecryption(key)

    private val preferencesRef = AtomicReference<Observable<Preferences>>()
    override fun providePreferences() : Observable<Preferences> = Observable.defer {
        this.preferencesRef.get()
            ?: run {
                val prefObservable = Observable.fromCallable<Preferences> {
                    val prefs = preferences
                    preferencesRef.set(Observable.just(prefs))
                    prefs
                }.subscribeOn(Schedulers.io()).share()

                if (preferencesRef.compareAndSet(null, prefObservable))
                    prefObservable
                else preferencesRef.get()
            }
    }

    override fun provideOkHttpClient() = httpClient

    override fun provideMoshi() : Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    override fun provideRemote() : MobileRemote {
        val moshi = provideMoshi()

        return AndroidMobileRemote(Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(provideOkHttpClient())
            .build()
            .create(MobileApi::class.java), moshi)
    }

    override fun provideStorage(): Storage {
        return FirebaseStorage()
    }

    override fun providePermissionsChecker(): PermissionsChecker = AndroidPermissionsChecker()
}

//Implements Android secure key storage
class AndroidSecureKeyProvider(private val context: Context) : SecureKeyProvider {

    override fun getKey(alias: String): ByteArray = if (!SecureKeyStore.hasKey(context, alias)) {
        SecureKeyStore.createKey(context, alias)
    } else {
        SecureKeyStore.getKey(context, alias)
    }
}

//Android implementation of encryption/decryption
class AndroidEncryptionDecryption(key: ByteArray) : EncryptionDecryption {
    val dec = EncryptDecrypt(key)

    override fun encrypt(toBeEncrypt: ByteArray): ByteArray = dec.encrypt(toBeEncrypt)

    override fun decrypt(value: ByteArray): ByteArray = dec.decrypt(value)
}

/**
 * Used to manage a single instance of a cookies storage
 */
object CookieStore {
    private val cookieStoreRef = AtomicReference<SimpleCookieJar>()

    fun get(): SimpleCookieJar = cookieStoreRef.get()
        ?: throw IllegalStateException("Cookies store has not been set.")

    fun set(value: SimpleCookieJar) {
        cookieStoreRef.getAndSet(value)
    }
}