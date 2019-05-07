package com.thomascook.instructorpricecomparison

/**
 * Collection of interfaces used by the app.
 */
//Used to abstract away encryption/decryption
interface EncryptionDecryption {
    fun encrypt(toBeEncrypt: ByteArray): ByteArray
    fun decrypt(value: ByteArray): ByteArray
}

//Used to abstract away secure key storage
interface SecureKeyProvider {
    //Get/create a key for the specified alias
    fun getKey(alias: String): ByteArray
}