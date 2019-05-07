package com.thomascook.instructorpricecomparison.application.remote

import android.content.Context
import android.content.SharedPreferences
import com.thomascook.instructorpricecomparison.application.EventReporter
import com.thomascook.core.EncryptDecrypt
import com.thomascook.core.SecureKeyStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.io.*


private const val AES_KEY_ALIAS = "com.thomascook.instructorcomparison"

/**
 * Function for getting initialisation vector and AES key for encrypting/decrypting preferences.
 */
private fun getEncryptionKey(context: Context): ByteArray {
    return if (!SecureKeyStore.hasKey(context, AES_KEY_ALIAS)) {
        SecureKeyStore.createKey(context, AES_KEY_ALIAS)
    } else {
        SecureKeyStore.getKey(context, AES_KEY_ALIAS)
    }
}

private fun createCookieKey(cookie: Cookie): String {
    return (if (cookie.secure()) "https" else "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name()
}

private const val TAG = "SimpleCookieJar"

class SimpleCookieJar(private val context: Context) : CookieJar {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CookieJarPersistence", Context.MODE_PRIVATE)

    //private val mCookiesSet = ArrayListMultimap.create<String, Cookie>()
    private val mCookiesSet = HashMap<String, HashMap<String, Cookie>>()
    private val mLock = Any()

    init {
        //Load from preferences
        try {

            val decrypts = EncryptDecrypt(getEncryptionKey(context))
            for ((_, value) in sharedPreferences.all) {
                val serializedCookie = value as String

                val cookie = SerializableCookie().decode(decrypts.decrypt(serializedCookie))

                synchronized(mLock) {
                    cookie?.domain()?.also {
                        val cookies = mCookiesSet[it] ?: run {
                            val newHash = HashMap<String, Cookie>()
                            mCookiesSet[it] = newHash
                            newHash
                        }
                        cookies[cookie.name()] = cookie
                    }
                }
            }
        } catch (error: Exception) {
            EventReporter.e(TAG, "Failed to load stored cookies.", error)
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host() ?: return
        if (cookies.isEmpty())
            return

        synchronized(mLock) {
            //We need to make sure that any cookie values with the same name get overridden.
            //Any new values get appended.
            val existingCookies = mCookiesSet[host] ?: run {
                val newHash = HashMap<String, Cookie>()
                mCookiesSet[host] = newHash
                newHash
            }
            cookies.forEach {
                existingCookies[it.name()] = it
            }

        }

        //Sync with the preferences
        val encrypts = try {
            EncryptDecrypt(getEncryptionKey(context))
        } catch (error: Exception) {
            return
        }

        val editor = sharedPreferences.edit()
        synchronized(mLock) {
            mCookiesSet.values.forEach {
                it.values.forEach {
                    val encoded = SerializableCookie().encode(it)
                    if (encoded !== null)
                        editor.putString(createCookieKey(it), encrypts.encrypt(encoded))
                }
            }
        }
        editor.apply()

    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host() ?: return emptyList()
        synchronized(mLock) {
            return mCookiesSet[host]?.values?.toList() ?: emptyList()
        }
    }

    fun clear() {
        synchronized(mLock) {
            mCookiesSet.clear()
        }
        sharedPreferences.edit().clear().apply()
    }
}

class SerializableCookie : Serializable {

    @Transient
    private var cookie: Cookie? = null

    fun encode(cookie: Cookie): String? {
        this.cookie = cookie

        val byteArrayOutputStream = ByteArrayOutputStream()
        var objectOutputStream: ObjectOutputStream? = null

        try {
            objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(this)
        } catch (e: IOException) {
            EventReporter.d(TAG, "IOException in encodeCookie", e)
            return null
        } finally {
            try {
                // Closing a ByteArrayOutputStream has no effect, it can be used later (and is used in the return statement)
                objectOutputStream?.close()
            } catch (e: IOException) {
                EventReporter.d(TAG, "Stream not closed in encodeCookie", e)
            }
        }

        return byteArrayToHexString(byteArrayOutputStream.toByteArray())
    }

    fun decode(encodedCookie: String): Cookie? {

        val bytes = hexStringToByteArray(encodedCookie)
        val byteArrayInputStream = ByteArrayInputStream(
            bytes)

        var cookie: Cookie? = null
        var objectInputStream: ObjectInputStream? = null
        try {
            objectInputStream = ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as SerializableCookie).cookie
        } catch (e: IOException) {
            EventReporter.d(TAG, "IOException in decodeCookie", e)
        } catch (e: ClassNotFoundException) {
            EventReporter.d(TAG, "ClassNotFoundException in decodeCookie", e)
        } finally {
            try {
                objectInputStream?.close()
            } catch (e: IOException) {
                EventReporter.d(TAG, "Stream not closed in decodeCookie", e)
            }
        }
        return cookie
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        cookie?.also {
            out.writeObject(it.name())
            out.writeObject(it.value())
            out.writeLong(if (it.persistent()) it.expiresAt() else NON_VALID_EXPIRES_AT)
            out.writeObject(it.domain())
            out.writeObject(it.path())
            out.writeBoolean(it.secure())
            out.writeBoolean(it.httpOnly())
            out.writeBoolean(it.hostOnly())
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val builder = Cookie.Builder()

        builder.name(`in`.readObject() as String)

        builder.value(`in`.readObject() as String)

        val expiresAt = `in`.readLong()
        if (expiresAt != NON_VALID_EXPIRES_AT) {
            builder.expiresAt(expiresAt)
        }

        val domain = `in`.readObject() as String
        builder.domain(domain)

        builder.path(`in`.readObject() as String)

        if (`in`.readBoolean())
            builder.secure()

        if (`in`.readBoolean())
            builder.httpOnly()

        if (`in`.readBoolean())
            builder.hostOnlyDomain(domain)

        cookie = builder.build()
    }

    companion object {
        private val TAG = SerializableCookie::class.java.simpleName

        private const val serialVersionUID = -8594045714036645534L

        /**
         * Using some super basic byte array &lt;-&gt; hex conversions so we don't
         * have to rely on any large Base64 libraries. Can be overridden if you
         * like!
         *
         * @param bytes byte array to be converted
         * @return string containing hex values
         */
        private fun byteArrayToHexString(bytes: ByteArray): String {
            val sb = StringBuilder(bytes.size * 2)
            for (element in bytes) {
                val v = element.toInt() and 0xff
                if (v < 16) {
                    sb.append('0')
                }
                sb.append(Integer.toHexString(v))
            }
            return sb.toString()
        }

        /**
         * Converts hex values from strings to byte array
         *
         * @param hexString string of hex-encoded values
         * @return decoded byte array
         */
        private fun hexStringToByteArray(hexString: String): ByteArray {
            val len = hexString.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character
                    .digit(hexString[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        private const val NON_VALID_EXPIRES_AT = -1L
    }
}