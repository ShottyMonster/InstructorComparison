package com.thomascook.instructorpricecomparison.application.preferences

import android.content.Context
import android.preference.PreferenceManager
import com.thomascook.instructorpricecomparison.EncryptionDecryption
import com.thomascook.core.ApplicationPreferences
import com.thomascook.instructorpricecomparison.UserDetails
import com.thomascook.instructorpricecomparison.preferences.Preferences
import java.io.Serializable

private const val PREF_USER = "UserDetails"

/**
 * MUST be called by inheriting classes to initialize shared preferences
 *
 * @param context Application context
 */
class AndroidPreferences(private val context: Context,
                         private val encryptionDecryption: EncryptionDecryption) : Preferences {

    /**
     * Function for storing serializable objects in encrypted form.
     * @param key Preference key used for storing
     * @param value Object that supports [Serializable] interface
     */
    private fun setPreferenceEnc(key: String, value: java.io.Serializable?): Preferences {
        if (value == null)
            applicationPreferences.remove(key)
        else {
            applicationPreferences.setSerializable(key, value) { toEncrypt ->
                encryptionDecryption.encrypt(toEncrypt)
            }
        }
        return this
    }

    /**
     * Function for retrieving encrypted [Serializable] objects
     * @param key Preference key.
     */
    private fun <T : java.io.Serializable> getPreferenceEnc(key: String): T? {
        return applicationPreferences.getSerializable(key) { toDecrypt ->
            encryptionDecryption.decrypt(toDecrypt)
        }
    }

    //Used to access application preferences
    private val applicationPreferences = ApplicationPreferences(PreferenceManager.getDefaultSharedPreferences(context))

    override fun apply() {
        this.applicationPreferences.apply()
    }

    private fun setPreference(key: String, value: String) {
        if (value.isEmpty())
            this.applicationPreferences.remove(key)
        else this.applicationPreferences.setString(key, value)
    }

    override var userDetails: UserDetails?
        get() = getPreferenceEnc(PREF_USER)
        set(value) {
            setPreferenceEnc(PREF_USER, value ?: "")
        }
}