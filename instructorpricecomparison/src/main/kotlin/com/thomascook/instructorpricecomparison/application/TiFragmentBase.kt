package com.thomascook.instructorpricecomparison.application

import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import net.grandcentrix.thirtyinch.TiFragment
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView

private const val KEY_STATE_BUNDLE = "state_bundle"

/**
 * Base class that support basic functionality
 */
abstract class TiFragmentBase<P : TiPresenter<V>, V : TiView> : TiFragment<P, V>() {
    //We store our intermediate state here
    protected lateinit var mStateBundle: Bundle
    protected val viewCompositeSubscription = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mStateBundle = savedInstanceState?.getBundle(KEY_STATE_BUNDLE) ?: Bundle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //Save the state of the fragment
        outState.putBundle(KEY_STATE_BUNDLE, this.mStateBundle)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        this.viewCompositeSubscription.clear()
        super.onDestroyView()
    }
}