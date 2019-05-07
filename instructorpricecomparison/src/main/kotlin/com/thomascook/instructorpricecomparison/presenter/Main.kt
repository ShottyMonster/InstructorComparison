package com.thomascook.instructorpricecomparison.presenter

import net.grandcentrix.thirtyinch.TiView
import java.util.*

/**
 * Contract for main app flow
 */
interface MainView : TiView {
    /**
     * Called to show the search for instructor view
     */
    fun showSearchForInstructor()

    /**
     * Called to show dialogue asking userDetails for location permission
     */
    fun showRequestFineLocation()
}

/**
 * Contract for main presenter
 */
abstract class MainPresenter : PresenterBase<MainView>() {
    /**
     * Called to inform the presenter that the userDetails has either granted or denied access to fine location
     */
    abstract fun onRequestFineLocationResult(granted: Boolean)
}