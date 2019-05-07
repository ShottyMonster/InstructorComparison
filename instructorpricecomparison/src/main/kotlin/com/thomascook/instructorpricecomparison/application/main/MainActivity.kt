package com.thomascook.instructorpricecomparison.application.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.thomascook.instructorpricecomparison.R
import com.thomascook.instructorpricecomparison.application.Injector
import com.thomascook.instructorpricecomparison.application.ViewContainer
import com.thomascook.instructorpricecomparison.presenter.MainPresenter
import com.thomascook.instructorpricecomparison.presenter.MainView
import net.grandcentrix.thirtyinch.TiActivity
import java.util.*
import com.thomascook.instructorpricecomparison.databinding.ViewSearchForInstructorBinding

private const val TAG = "MainActivity"
private const val TAG_VIEW_CONTAINER = "ViewContainer"
private const val TAG_PROGRESS = "Progress"
private const val PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 0
private const val TAG_SEARCH_FOR_INSTRUCTOR = "SearchForInstructor"
private const val FRAGMENT_TAG_MAP = "FragmentMap"
private const val STACK_HOME = "Home"
private const val DEFAULT_ZOOM_METERS = 12f

/**
 * Main activity of the app
 */
class MainActivity : TiActivity<MainPresenter, MainView>(), MainView, OnMapReadyCallback {

    // View container for managing view transitions
    private val viewContainer = ViewContainer()

    // Used to queue runnables that should only be processed after onStart has been called
    private val postponedRunnables = Stack<() -> Unit>()

    override fun providePresenter() = AndroidMainPresenter(this, Injector.provider)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_Application)

        viewContainer.attach(findViewById(android.R.id.content))
    }

    override fun onResume() {
        super.onResume()
        while (postponedRunnables.isNotEmpty()) {
            postponedRunnables.pop().invoke()
        }
    }

    override fun showRequestFineLocation() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        postponedRunnables.add {
            if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                presenter.onRequestFineLocationResult(true)
            } else {
                presenter.onRequestFineLocationResult(false)
            }
        }
    }

    override fun showSearchForInstructor() {
        if (viewContainer.currentViewBinding is ViewSearchForInstructorBinding)
            return

        val viewBinding = ViewSearchForInstructorBinding.inflate(LayoutInflater.from(this))

        viewContainer.transitionToScene(viewBinding, TAG_SEARCH_FOR_INSTRUCTOR)

        if (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_MAP) === null) {
            val fragment = SupportMapFragment()

            fragment.getMapAsync(this@MainActivity)

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace((viewContainer.currentViewBinding as ViewSearchForInstructorBinding).fragmentHolder.id, fragment, FRAGMENT_TAG_MAP)
                .addToBackStack(STACK_HOME)
                .commit()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        LocationServices.getFusedLocationProviderClient(this).apply {
            lastLocation.addOnSuccessListener { currentLocation ->
                val geoLoc = LatLng(currentLocation.latitude, currentLocation.longitude)
                googleMap.addMarker(MarkerOptions().position(geoLoc).title("Your Location"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoLoc, DEFAULT_ZOOM_METERS))
            }
        }
    }
}