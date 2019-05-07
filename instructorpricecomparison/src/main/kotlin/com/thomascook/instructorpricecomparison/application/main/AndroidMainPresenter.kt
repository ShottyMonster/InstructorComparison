package com.thomascook.instructorpricecomparison.application.main

import android.content.Context
import com.thomascook.instructorpricecomparison.application.InjectorProvider
import com.thomascook.instructorpricecomparison.presenter.MainPresenter
import com.thomascook.instructorpricecomparison.presenter.MainView
import com.thomascook.instructorpricecomparison.presenter.ViewState
import java.util.*

private const val TAG = "AndroidMainPresenter"

/**
 * Type to represent unspecified view state
 */
private class UnspecifiedViewState : ViewState

/**
 * Type to represent the search for instructor view state
 */
private class SearchForInstructorViewState : ViewState

class AndroidMainPresenter(context: Context, injectorProvider: InjectorProvider) : MainPresenter() {

    // Stack of view states
    private val viewStateStack = Stack<ViewState>()

    // Reference to app context
    private val appContext = context.applicationContext

    // Injected values
    private val permissionsChecker = injectorProvider.providePermissionsChecker()

    override fun onAttachView(view: MainView) {
        super.onAttachView(view)

        // Get the current view state. If there isn't one, get an UnspecifiedViewState
        val viewState: ViewState = if (viewStateStack.isNotEmpty()) {
            viewStateStack.peek()
        } else {
            UnspecifiedViewState()
        }

        // Check if we have location permissions and, if not, request them, else show network selection
        if (!permissionsChecker.canAccessFineLocation(this.appContext)) {
            view.showRequestFineLocation()
        } else {
            when (viewState) {
                is UnspecifiedViewState -> {
                    viewStateStack.add(SearchForInstructorViewState())
                    switchToViewState(viewStateStack.peek())
                }
                else -> {
                    switchToViewState(viewState)
                }
            }
        }
    }

    override fun onRequestFineLocationResult(granted: Boolean) {
        if (granted) {
            viewStateStack.add(SearchForInstructorViewState())
            switchToViewState(viewStateStack.peek())
        }
    }

    private fun switchToViewState(viewState: ViewState) {
        this.view?.also { view ->
            when (viewState) {
                is SearchForInstructorViewState -> view.showSearchForInstructor()
            }
        }
    }
}