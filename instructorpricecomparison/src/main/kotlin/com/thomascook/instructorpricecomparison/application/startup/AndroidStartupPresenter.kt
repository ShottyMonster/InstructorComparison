package com.thomascook.instructorpricecomparison.application.startup

import android.app.Activity.RESULT_OK
import android.content.Context
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thomascook.instructorpricecomparison.*
import com.thomascook.instructorpricecomparison.application.EventReporter
import com.thomascook.instructorpricecomparison.application.InjectorProvider
import com.thomascook.instructorpricecomparison.presenter.StartupPresenter
import com.thomascook.instructorpricecomparison.presenter.StartupView
import com.thomascook.instructorpricecomparison.presenter.ViewState
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalStateException
import java.util.*


private const val TAG = "AndroidStartupPresenter"

/**
 * View state for login form
 */
private class LoginViewState : ViewState

/**
 * Type to represent the welcome view state
 */
private class WelcomeViewState : ViewState

/**
 * Type to represent the instructor details view state
 */
private class InstructorDetailsViewState: ViewState

/**
 * Type to represent the select instructor times view state
 */
private class SelectInstructorTimesViewState : ViewState

/**
 * Type to represent the learner details view state
 */
private class LearnerDetailsViewState : ViewState

/**
 * Type to represent the select learner times view state
 */
private class SelectLearnerTimesViewState : ViewState

private interface UserRepository {
    fun addUser(user: User) : Completable
    fun getUser() : Single<User>
}

private class InMemoryUserRepository : UserRepository {
    private var user: User? = null

    override fun addUser(user: User): Completable {
        return Completable.fromAction {
            this.user = user
        }
    }

    override fun getUser(): Single<User> {
        return Single.fromCallable {
            this.user ?: throw IllegalStateException("Attempted to get null user")
        }
    }
}


class AndroidStartupPresenter(context: Context, injectorProvider: InjectorProvider) : StartupPresenter() {

    // Stack of view states
    private val viewStateStack = Stack<ViewState>()

    // Reference to app context
    private val appContext = context.applicationContext

    // Injected values
    private val preferences = injectorProvider.providePreferences()
    private val storage = injectorProvider.provideStorage()

    // Disposables
    private val prefsDisposable = DisposableWrapper(viewVisibleDisposables)
    private val firestoreDisposable = DisposableWrapper(viewVisibleDisposables)
    private val inMemoryRepoDisposable = DisposableWrapper(viewVisibleDisposables)

    // Hash map of view state -> walkthrough step for instructor and learner
    private val instructorWalkthroughStepMap = hashMapOf(Pair(INSTRUCTOR_STEPS.PROVIDE_DETAILS, InstructorDetailsViewState::class.java))
    private val learnerWalkthroughStepMap = hashMapOf(Pair(LEARNER_STEPS.PROVIDE_DETAILS, LearnerDetailsViewState::class.java))

    // In memory user repository
    private val inMemoryUserRepository = InMemoryUserRepository()

    override fun onAttachView(view: StartupView) {
        super.onAttachView(view)

        // Go to prefs and see there is already a userDetails
        // If there isn't, create a default one
        // Then emit the userDetails downstream
        prefsDisposable.disposable = preferences.map { prefs ->
            if (prefs.userDetails !== null) {
                prefs.userDetails
                    ?: throw IllegalStateException("UserDetails was null after checking it was not null")
            } else {
                prefs.userDetails = UserDetails()
                prefs.apply()
                prefs.userDetails
                    ?: throw IllegalStateException("UserDetails was null after creating it and calling apply() on prefs")
            }
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe ({ user ->
            // Dispose
            prefsDisposable.disposable = null

            // If the userDetails is currently logging in then early return
            if (user.isCurrentlyLoggingIn)
                return@subscribe

            // Get the current walkthrough step
            val currentWalkthroughStep = (user.currentWalkthroughStep as? LEARNER_STEPS)
                ?: user.currentWalkthroughStep as? INSTRUCTOR_STEPS

            // Work out if this userDetails has already completed walkthrough
            val hasCompletedWalkthrough = currentWalkthroughStep == LEARNER_STEPS.COMPLETED
                    || currentWalkthroughStep == INSTRUCTOR_STEPS.COMPLETED

            if (hasCompletedWalkthrough && user.uid !== null) {
                // The userDetails has already completed the walkthrough
                // So just navigate to main
                view.navigateToMain()
            } else {
                if (user.uid === null) {
                    // The userDetails has never logged in
                    // So show them the welcome page
                    viewStateStack.push(LoginViewState())
                    switchToViewState(viewStateStack.peek())
                } else {
                    // Work out if the userDetails has been welcomed and if they haven't add welcome view state to stack
                    if (user.currentWalkthroughStep === null) {
                        viewStateStack.push(WelcomeViewState())
                        switchToViewState(viewStateStack.peek())
                    } else {
                        // Work out if the userDetails is an instructor or a learner
                        val isLearner = currentWalkthroughStep is LEARNER_STEPS

                        // Get the class of the expected walkthrough step
                        val expectedStep = when(isLearner) {
                            true -> learnerWalkthroughStepMap[currentWalkthroughStep] ?: LearnerDetailsViewState::class.java
                            else -> instructorWalkthroughStepMap[currentWalkthroughStep] ?: InstructorDetailsViewState::class.java
                        }

                        // The userDetails has logged in before but has not completed the walkthrough
                        // Find the current walkthrough step on the view stack if it exists
                        var currentWalkthroughStepViewState: ViewState? = null
                        while (viewStateStack.isNotEmpty()) {
                            val currentViewState = viewStateStack.peek()
                            if (currentViewState::class.java === expectedStep) {
                                currentWalkthroughStepViewState = currentViewState
                            }
                        }

                        // If the stack contained a view state matching the users current walkthrough step then show it
                        // else push the current walkthrough step onto the stack and show that
                        if (currentWalkthroughStepViewState !== null) {
                            switchToViewState(currentWalkthroughStepViewState)
                        } else {
                            viewStateStack.push(expectedStep.newInstance())
                            switchToViewState(viewStateStack.peek())
                        }
                    }
                }
            }
        }, { error ->
            // Dispose
            prefsDisposable.disposable = null

            // Report the error
            EventReporter.e(TAG, "UserDetails was unable to be retrieved from prefs", error)
        })
    }

    override fun onUserLoggingIn() {
        if (prefsDisposable.isNotDisposed)
            return

        prefsDisposable.disposable = preferences.map { prefs ->
            val currentUser = prefs.userDetails
            val updatedUser = UserDetails(currentUser?.uid, currentUser?.currentWalkthroughStep, true)
            prefs.userDetails = updatedUser
            prefs.apply()
            prefs.userDetails
                ?: throw IllegalStateException("UserDetails was null after creating it and calling apply() on prefs")
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ user ->
                // Dispose
                prefsDisposable.disposable = null

                // Report the event
                EventReporter.d(TAG, "Updated userDetails is logging in flag")
            }, { error ->
                // Dispose
                prefsDisposable.disposable = null

                // Report the error
                EventReporter.e(TAG, "UserDetails was unable to be retrieved from prefs", error)
            })
    }

    override fun onSignInComplete(response: IdpResponse?, resultCode: Int) {
        if (prefsDisposable.isNotDisposed)
            return

        prefsDisposable.disposable = preferences.map { prefs ->
            if (resultCode == RESULT_OK) {
                // The auth was successful
                val user = FirebaseAuth.getInstance().currentUser
                if (user !== null) {
                    // The userDetails was not null
                    // Update the userDetails object in prefs
                    val currentUser = prefs.userDetails
                    val updatedUser = UserDetails(user.uid, currentUser?.currentWalkthroughStep, false)
                    prefs.userDetails = updatedUser
                    prefs.apply()
                } else {
                    // The userDetails was null which means auth failed
                    throw UserAuthException("UserDetails failed to authorize")
                }
            } else {
                if (response === null) {
                    // UserDetails cancelled flow
                    throw UserAuthException("UserDetails cancelled the authorization flow")
                } else {
                    // Error occurred
                    throw UserAuthException("Authorization failed")
                }
            }
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            // Dispose
            prefsDisposable.disposable = null

            // Navigate to first step of the userDetails journey
            viewStateStack.push(WelcomeViewState())
            switchToViewState(viewStateStack.peek())
        }, { error ->
            // Dispose
            prefsDisposable.disposable = null

            // Report the error
            EventReporter.e(TAG, "UserDetails sign in failed", error)
        })
    }

    override fun onUserTypeChosen(isInstructor: Boolean) {
        if (this.prefsDisposable.isNotDisposed)
            return

        this.prefsDisposable.disposable = preferences.map { prefs ->
            // Update the current userDetails in prefs to select the correct the walkthrough steps
            val currentUser = prefs.userDetails
            val updatedUser = UserDetails(currentUser?.uid, if (isInstructor) INSTRUCTOR_STEPS.PROVIDE_DETAILS else LEARNER_STEPS.PROVIDE_DETAILS)
            prefs.userDetails = updatedUser
            prefs.apply()
            prefs.userDetails
                ?: throw IllegalStateException("UserDetails was null after creating it and calling apply() on prefs")
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ user ->
                // Dispose
                prefsDisposable.disposable = null

                // Get the current walkthrough step
                val currentWalkthroughStep = user.currentWalkthroughStep

                // Work out if the userDetails is an instructor or a learner
                val isLearner = currentWalkthroughStep is LEARNER_STEPS

                // Get the class of the expected walkthrough step
                val expectedStep = when(isLearner) {
                    true -> learnerWalkthroughStepMap[currentWalkthroughStep] ?: LearnerDetailsViewState::class.java
                    else -> instructorWalkthroughStepMap[currentWalkthroughStep] ?: InstructorDetailsViewState::class.java
                }

                // Push the view state onto the stack and transition to that view
                viewStateStack.push(expectedStep.newInstance())
                switchToViewState(viewStateStack.peek())
            }, { error ->
                // Dispose
                prefsDisposable.disposable = null

                // Report the error
                EventReporter.e(TAG, "UserDetails was unable to be retrieved from prefs", error)
            })
    }

    override fun onInstructorDetailsProvided(forename: CharSequence, surname: CharSequence,
                                             dobDay: Int, dobMonth: Int, dobYear: Int) {
        if (inMemoryRepoDisposable.isNotDisposed)
            return

        view?.showProgress(appContext.getString(R.string.please_wait))

        inMemoryRepoDisposable.disposable = preferences.flatMapCompletable { prefs ->
            val userDetails = prefs.userDetails
                ?: return@flatMapCompletable Completable.error(IllegalStateException(
                    "Attempted to create instructor when no user was in prefs"))

            val dateOfBirth = Calendar.getInstance().apply {
                set(Calendar.YEAR, dobYear)
                set(Calendar.MONTH, dobMonth)
                set(Calendar.DAY_OF_YEAR, dobDay)
            }.time

            val instructor = Instructor(userDetails, "", forename.toString(), surname.toString(), dateOfBirth)

            inMemoryUserRepository.addUser(instructor)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Switch to the instructor times view
                viewStateStack.push(SelectInstructorTimesViewState())
                switchToViewState(viewStateStack.peek())
            }, { error ->
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Report the error
                EventReporter.e(TAG, "Failed to add instructor to memory cache")
            })
    }

    override fun onLearnerDetailsProvided(forename: CharSequence, surname: CharSequence,
                                          dobDay: Int, dobMonth: Int, dobYear: Int) {
        if (inMemoryRepoDisposable.isNotDisposed)
            return

        view?.showProgress(appContext.getString(R.string.please_wait))

        inMemoryRepoDisposable.disposable = preferences.flatMapCompletable { prefs ->
            val userDetails = prefs.userDetails
                ?: return@flatMapCompletable Completable.error(IllegalStateException(
                    "Attempted to create instructor when no user was in prefs"))

            val dateOfBirth = Calendar.getInstance().apply {
                set(Calendar.YEAR, dobYear)
                set(Calendar.MONTH, dobMonth)
                set(Calendar.DAY_OF_YEAR, dobDay)
            }.time

            val learner = Learner(userDetails, "", forename.toString(), surname.toString(), dateOfBirth)

            inMemoryUserRepository.addUser(learner)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Switch to the learner times view
                viewStateStack.push(SelectLearnerTimesViewState())
                switchToViewState(viewStateStack.peek())
            }, { error ->
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Report the error
                EventReporter.e(TAG, "Failed to add learner to memory cache")
            })
    }

    override fun onInstructorTimesProvided(availableFromHour: Int, availableFromMinute: Int, availableToHour: Int,
                                           availableToMinute: Int, sunday: Boolean, monday: Boolean, tuesday: Boolean,
                                           wednesday: Boolean, thursday: Boolean, friday: Boolean, saturday: Boolean) {

        if (this.firestoreDisposable.isNotDisposed)
            return

        view?.showProgress(appContext.getString(R.string.please_wait))

        this.firestoreDisposable.disposable = inMemoryUserRepository.getUser().flatMapCompletable { user ->
            if (user !is Instructor)
                return@flatMapCompletable Completable.error(IllegalStateException("Attempted to retrieve a user but it wasn't an instructor"))

            val updatedInstructor = Instructor(user.userDetails, user.uid, user.forename, user.surname, user.dateOfBirth,
                availableFromHour, availableFromMinute, availableToHour, availableToMinute, sunday, monday, tuesday, wednesday, thursday, friday, saturday)

            storage.addInstructor(updatedInstructor).flatMapCompletable {
                inMemoryUserRepository.addUser(updatedInstructor)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Navigate to main
                view?.navigateToMain()
            }, { error ->
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Report the error
                EventReporter.e(TAG, "Failed to add instructor to firebase")
            })
    }

    override fun onLearnerTimesProvided(availableFromHour: Int, availableFromMinute: Int, availableToHour: Int,
                                           availableToMinute: Int, sunday: Boolean, monday: Boolean, tuesday: Boolean,
                                           wednesday: Boolean, thursday: Boolean, friday: Boolean, saturday: Boolean) {

        if (this.firestoreDisposable.isNotDisposed)
            return

        view?.showProgress(appContext.getString(R.string.please_wait))

        this.firestoreDisposable.disposable = inMemoryUserRepository.getUser().flatMapCompletable { user ->
            if (user !is Learner)
                return@flatMapCompletable Completable.error(IllegalStateException("Attempted to retrieve a user but it wasn't a learner"))

            val updatedLearner = Learner(user.userDetails, user.uid, user.forename, user.surname, user.dateOfBirth,
                availableFromHour, availableFromMinute, availableToHour, availableToMinute, sunday, monday, tuesday, wednesday, thursday, friday, saturday)

            storage.addLearner(updatedLearner).flatMapCompletable {
                inMemoryUserRepository.addUser(it)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Navigate to main
                view?.navigateToMain()
            }, { error ->
                // Dispose
                inMemoryRepoDisposable.disposable = null

                // Report the error
                EventReporter.e(TAG, "Failed to add learner to firebase")
            })
    }

    private fun switchToViewState(viewState: ViewState) {
        this.view?.also { view ->
            when (viewState) {
                is LoginViewState -> view.showLoginForm()
                is WelcomeViewState -> view.showWelcomeForm()
                is LearnerDetailsViewState -> view.showLearnerDetailsForm()
                is SelectLearnerTimesViewState -> view.showSelectLearnerTimes()
                is InstructorDetailsViewState -> view.showInstructorDetailsForm()
                is SelectInstructorTimesViewState -> view.showSelectInstructorTimes()
            }
        }
    }
}