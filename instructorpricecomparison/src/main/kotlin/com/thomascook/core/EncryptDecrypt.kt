package com.thomascook.core

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Helper function for returning existing initialisation vector array or creating a new one.
 */
private fun createOrGetIVByteArray(ivArray: ByteArray?, blockSize: Int): ByteArray {
    return if (ivArray != null) {
        ivArray
    } else {
        val ivData = ByteArray(blockSize)
        val rnd = SecureRandom.getInstance("SHA1PRNG")
        rnd.nextBytes(ivData)
        ivData
    }
}

/**
 * Used for encryption / decryption
 */
class EncryptDecrypt(aesKey: ByteArray, initialisationVector: ByteArray? = null) {

    constructor(aesKey: String, initialisationVector: String) :
            this(aesKey.toByteArray(), initialisationVector.toByteArray())

    private val ivArray: ByteArray? = initialisationVector
    private val secretKeySpec: SecretKeySpec = SecretKeySpec(aesKey, "AES")
    private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

    @Throws(Exception::class)
    fun encrypt(toBeEncrypt: String): String = try {
        val encrypted = encrypt(toBeEncrypt.toByteArray())
        Base64.encodeToString(encrypted, Base64.DEFAULT)

    } catch (error: Exception) {
        throw Exception(error)
    }

    @Throws(Exception::class)
    fun decrypt(encrypted: String): String = try {
        val decoded = Base64.decode(encrypted, Base64.DEFAULT)
        String(decrypt(decoded))
    } catch (error: Exception) {
        throw Exception(error)
    }

    @Throws(Exception::class)
    fun encrypt(toBeEncrypt: ByteArray): ByteArray = try {
        val ivData = createOrGetIVByteArray(ivArray, cipher.blockSize)

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(ivData))
        val encryptedValue = cipher.doFinal(toBeEncrypt)
        if (ivArray != null) {
            //Don't pre-pend iv vector to the final result
            encryptedValue
        } else {
            //Store IV + encrypted value
            ivData + encryptedValue
        }
    } catch (error: Exception) {
        throw Exception(error)
    }

    @Throws(Exception::class)
    fun decrypt(value: ByteArray): ByteArray = try {
        val ivData: ByteArray
        val encryptedValue: ByteArray
        if (ivArray == null) {
            //Looks like IV is prefixing the value data
            val blockSize = cipher.blockSize
            ivData = value.copyOfRange(0, blockSize)
            encryptedValue = value.copyOfRange(blockSize, value.size)
        } else {
            //used the provided iv for decryption
            ivData = ivArray
            encryptedValue = value
        }

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(ivData))
        cipher.doFinal(encryptedValue)
    } catch (error: Exception) {
        throw Exception(error)
    }
}