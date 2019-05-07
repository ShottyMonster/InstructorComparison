package com.thomascook.instructorpricecomparison.presenter

import com.firebase.ui.auth.IdpResponse
import net.grandcentrix.thirtyinch.TiView

/**
 * Contract for startup flow
 */
interface StartupView : TiView {
    /**
     * Called to show the welcome form
     */
    fun showWelcomeForm()

    /**
     * Called to show the login form
     */
    fun showLoginForm()

    /**
     * Called to show the learner details form
     */
    fun showLearnerDetailsForm()

    /**
     * Called to show the select learner times view
     */
    fun showSelectLearnerTimes()

    /**
     * Called to show the instructor details form
     */
    fun showInstructorDetailsForm()

    /**
     * Called to show the select instructor times view
     */
    fun showSelectInstructorTimes()

    /**
     * Called to show a progress spinner with the given [message]
     */
    fun showProgress(message: CharSequence)

    /**
     * Called to navigate away to the main activity
     */
    fun navigateToMain()
}

/**
 * Contract for startup presenter
 */
abstract class StartupPresenter : PresenterBase<StartupView>() {
    /**
     * Called to inform the presenter that the userDetails is logging in
     */
    abstract fun onUserLoggingIn()

    /**
     * Called to inform the presenter that the user has chosen their type
     */
    abstract fun onUserTypeChosen(isInstructor: Boolean)

    /**
     * Called to inform the presenter that the sign in process was complete
     */
    abstract fun onSignInComplete(response: IdpResponse?, resultCode: Int)

    /**
     * Called to inform the presenter that the user has provided learner details
     */
    abstract fun onLearnerDetailsProvided(forename: CharSequence,  surname: CharSequence,  dobDay: Int,  dobMonth: Int, dobYear: Int)

    /**
     * Called to inform the presenter that the user has provided learner times
     */
    abstract fun onLearnerTimesProvided(availableFromHour: Int, availableFromMinute: Int, availableToHour: Int, availableToMinute: Int,
                                        sunday: Boolean, monday: Boolean, tuesday: Boolean, wednesday: Boolean, thursday: Boolean, friday: Boolean, saturday: Boolean)

    /**
     * Called to inform the presenter that the userDetails has provided instructor details
     */
    abstract fun onInstructorDetailsProvided(forename: CharSequence,  surname: CharSequence,  dobDay: Int,  dobMonth: Int, dobYear: Int)

    /**
     * Called to inform the presenter that the user has provided instructor times
     */
    abstract fun onInstructorTimesProvided(availableFromHour: Int, availableFromMinute: Int, availableToHour: Int, availableToMinute: Int,
                                           sunday: Boolean, monday: Boolean, tuesday: Boolean, wednesday: Boolean, thursday: Boolean, friday: Boolean, saturday: Boolean)
}