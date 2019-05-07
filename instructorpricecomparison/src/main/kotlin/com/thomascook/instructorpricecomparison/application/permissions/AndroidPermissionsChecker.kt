package com.thomascook.instructorpricecomparison.application.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.thomascook.instructorpricecomparison.permissions.PermissionsChecker

class AndroidPermissionsChecker : PermissionsChecker {
    override fun canAccessFineLocation(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}