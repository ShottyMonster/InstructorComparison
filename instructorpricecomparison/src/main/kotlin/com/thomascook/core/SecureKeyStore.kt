package com.thomascook.core

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import android.util.Log
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

private const val ANDROID_KEYSTORE_INSTANCE = "AndroidKeyStore"
private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
private const val KEY_ALGORITHM_RSA = "RSA"
private const val PRIVATE_RSA_KEY = "PrivateRsaKey"

/**
 * Get keystore share preferences
 */
private fun getSharedPreferences(context: Context): SharedPreferences =
    context.getSharedPreferences("secure", MODE_PRIVATE)

/**
 * Used to return keystore with loaded data
 */
private fun getKeyStore(): KeyStore {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_INSTANCE)
    keyStore.load(null)
    return keyStore
}

// Generates an RSA key pair used to encryptWithRsaKey keys stored in prefs
private fun generateRsaKeyPair(context: Context, keyStore: KeyStore, rsaKeyAlias: String): KeyPair {
    // Check if it already contains the key
    if (keyStore.containsAlias(rsaKeyAlias)) {
        throw KeyStoreException("Key for alias $rsaKeyAlias already exists.")
    }

    // Start and end dates for the RSA key pair
    val startDate = Calendar.getInstance()
    val endDate = Calendar.getInstance()
    endDate.add(Calendar.YEAR, 30)

    // Create the key pair spec
    @Suppress("DEPRECATION")
    val keySpec = android.security.KeyPairGeneratorSpec.Builder(context)
        .setAlias(rsaKeyAlias)
        .setSubject(X500Principal("CN=$rsaKeyAlias"))
        .setSerialNumber(BigInteger.valueOf(1337))
        .setStartDate(startDate.time)
        .setEndDate(endDate.time)
        .build()

    // Configure and initialize keygen
    val keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, ANDROID_KEYSTORE_INSTANCE)
    keyPairGen.initialize(keySpec)

    // Return key pair to spec
    return keyPairGen.generateKeyPair()
}

// Returns the correct android provider (based on API level)
// See: https://stackoverflow.com/questions/34265943/android-decryption-error
private fun androidProviderCompat(): String =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        "AndroidOpenSSL"
    } else {
        "AndroidKeyStoreBCWorkaround"
    }

// Encrypts the passed bytes rsaKey the passed certificate
private fun encryptWithRsaKey(toEncrypt: ByteArray, rsaKey: Key): ByteArray {
    // Configure and init cipher
    val inputCipher = Cipher.getInstance(RSA_MODE, androidProviderCompat())

    inputCipher.init(Cipher.ENCRYPT_MODE, rsaKey)
    return inputCipher.doFinal(toEncrypt)
}

// Decrypts the passed bytes key the passed private key
private fun decrypt(toDecrypt: ByteArray, key: Key): ByteArray {
    // Configure and initialize the output cipher
    val outputCipher = Cipher.getInstance(RSA_MODE, androidProviderCompat())

    outputCipher.init(Cipher.DECRYPT_MODE, key)
    return outputCipher.doFinal(toDecrypt)
}

private const val TAG = "SecureKeyStore"

/**
 * Object provides methods for securely storing aes keys.
 */
object SecureKeyStore {

    // Deletes the key with the specified alias from preferences
    // Returns true if the operation was successful
    fun deleteKey(context: Context, alias: String): Boolean =
        try {
            // Get the preferences and try to delete the key
            getSharedPreferences(context).apply {
                edit().remove(alias).apply()
            }
            true
        } catch (ex: Exception) {
            false
        }

    // Returns true if the preferences contains the specified alias
    fun hasKey(context: Context, alias: String): Boolean = getSharedPreferences(context).contains(alias)

    // Creates a key with the specified alias
    fun createKey(context: Context, alias: String, rsaKeyAlias: String = PRIVATE_RSA_KEY): ByteArray {
        try {
            // Get system key store
            val keyStore = getKeyStore()

            // Get or create RSA public key used to encode generated AES key.
            val rsaKey: Key = if (keyStore.containsAlias(rsaKeyAlias)) {
                keyStore.getCertificate(rsaKeyAlias)?.publicKey
                    ?: throw IllegalStateException("RSA public key could not be read.")
            } else {
                // If keystore does not contain RSA key the generate one
                generateRsaKeyPair(context, keyStore, rsaKeyAlias).public
            }

            // Generate the AES key encrypted with the public RSA key
            // Get 32 random bytes
            val aesKey = ByteArray(32)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(aesKey)

            //Store the generated aes key
            //First encryptWithRsaKey it
            val aesKeyEncrypted = encryptWithRsaKey(aesKey, rsaKey)

            //Then store the key in preferences in encrypted form
            getSharedPreferences(context).apply {
                edit().putString(alias, Base64.encodeToString(aesKeyEncrypted, Base64.DEFAULT)).apply()
            }

            //Return the un-encrypted form of the key
            return aesKey

        } catch (ex: Exception) {
            Log.d(TAG, "Failed to generate the key", ex)
            throw Exception(ex)
        }
    }

    /**
     * Gets the key with the specified alias
     * @param context  Reference to a context
     * @param alias  key alias to be retrieved.
     * @param rsaKeyAlias rsa key alias used during key with alias creation.
     */
    fun getKey(context: Context, alias: String, rsaKeyAlias: String = PRIVATE_RSA_KEY): ByteArray {
        val aesKeyEncoded = getSharedPreferences(context).getString(alias, null)
            ?: throw IllegalArgumentException("Key '$alias' could not be found.")

        val aesKeyEncrypted = Base64.decode(aesKeyEncoded, Base64.DEFAULT)

        // Get system key store
        val keyStore = getKeyStore()

        // Get the private RSA key entry used to decrypt the decoded key
        val privateRsaKey = keyStore.getKey(rsaKeyAlias, null)
            ?: throw IllegalStateException("RSA private key couldn't be read.")

        // Return decrypted key using rsa private key to decrypt aes encrypted key
        return decrypt(aesKeyEncrypted, privateRsaKey)
    }
}