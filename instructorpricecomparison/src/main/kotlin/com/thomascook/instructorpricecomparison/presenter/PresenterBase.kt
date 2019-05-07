package com.thomascook.instructorpricecomparison.presenter

import io.reactivex.disposables.CompositeDisposable
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView
import java.util.*

/**
 * Base class for presenters
 */
abstract class PresenterBase<V : TiView> : TiPresenter<V>() {
    //Used to clean up pending subscriptions/disposables
    protected val disposables = CompositeDisposable()

    //Used to cleanup subscriptions when view becomes inactive
    protected val viewVisibleDisposables = CompositeDisposable()

    override fun onDetachView() {
        viewVisibleDisposables.clear()
        super.onDetachView()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}

/**
 * Presenter holder class implemented by fragments that don't have their own presenter.
 */
class TiPresenterHolder : TiPresenter<TiView>()

/**
 * Base interface for view state
 * So we can create a stack of them
 */
interface ViewState

/**
 * Interface for navigable fragments
 */
interface Navigable {
    /**
     * Called to notify the rest of the app that the userDetails wishes to cancel the view
     */
    fun notifyCancelled()

    /**
     * Called to go back
     */
    fun goBack()
}

//Get the view state at the top. It should be of expected type [T]. If it is -
// pop it from the view stack
inline fun <reified T : ViewState> Stack<ViewState>.popViewState(): T? =
    if (this.isNotEmpty() && this.peek() is T) {
        this.pop() as T
    } else null

//Get the view state at the top. It should be of expected type [T]
inline fun <reified T : ViewState> Stack<ViewState>.getExpectedCurrentViewState(): T? =
    if (this.isNotEmpty() && this.peek() is T) {
        this.peek() as T
    } else null