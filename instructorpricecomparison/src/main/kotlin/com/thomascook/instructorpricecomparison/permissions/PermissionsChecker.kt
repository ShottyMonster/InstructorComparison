package com.thomascook.instructorpricecomparison.permissions

import android.content.Context

/**
 * API for accesssing permissions
 */
interface PermissionsChecker {
    fun canAccessFineLocation(context: Context) : Boolean
}