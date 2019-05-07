package com.thomascook.instructorpricecomparison.preferences

import com.thomascook.instructorpricecomparison.UserDetails

interface Preferences {
    /**
     * Apply any outstanding changes
     */
    fun apply()

    /**
     * The userDetails logged into the app, or null if not
     */
    var userDetails: UserDetails?
}