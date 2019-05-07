package com.thomascook.instructorpricecomparison.application.startup

import android.content.Intent
import com.thomascook.instructorpricecomparison.R
import android.os.Bundle
import android.view.LayoutInflater
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.thomascook.instructorpricecomparison.application.Injector
import com.thomascook.instructorpricecomparison.application.ViewContainer
import com.thomascook.instructorpricecomparison.databinding.*
import com.thomascook.instructorpricecomparison.presenter.StartupPresenter
import com.thomascook.instructorpricecomparison.presenter.StartupView
import net.grandcentrix.thirtyinch.TiActivity
import com.thomascook.instructorpricecomparison.application.main.MainActivity
import java.util.*

private const val TAG = "StartupActivity"
private const val TAG_WELCOME_FORM = "WelcomeForm"
private const val TAG_LEARNER_DETAILS_FORM = "LearnerDetailsForm"
private const val TAG_INSTRUCTOR_DETAILS_FORM = "InstructorDetailsForm"
private const val TAG_LEARNER_TIMES = "LearnerTimes"
private const val TAG_INTSTRUCTOR_TIMES = "InstructorTimes"
private const val TAG_PROGRESS = "Progress"
private const val FRAGMENT_STACK_HOME = "FragStackHome"
private const val FRAGMENT_DATE_PICKER = "DatePicker"
private const val RC_SIGN_IN = 1

/**
 * First activity started by the app
 */
class StartupActivity : TiActivity<StartupPresenter, StartupView>(), StartupView {

    // View container for managing view transitions
    private val viewContainer = ViewContainer()

    // Used to queue runnables that should only be processed after onStart has been called
    private val postponedRunnables = Stack<() -> Unit>()

    override fun providePresenter() = AndroidStartupPresenter(this, Injector.provider)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_Application)

        viewContainer.attach(findViewById(android.R.id.content))
    }

    override fun showWelcomeForm() {
        if (viewContainer.currentViewBinding is ViewWelcomeFormBinding)
            return

        val viewBinding = ViewWelcomeFormBinding.inflate(LayoutInflater.from(this)).apply {
            this.instructorBtn.setOnClickListener {
                presenter.onUserTypeChosen(true)
            }
            this.learnerBtn.setOnClickListener {
                presenter.onUserTypeChosen(false)
            }
        }

        viewContainer.transitionToScene(viewBinding, TAG_WELCOME_FORM)
    }

    override fun showLoginForm() {
        // Create list of providers
        val providers = listOf(AuthUI.IdpConfig.EmailBuilder().build())

        // Start Firebase auth UI
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build(),
            RC_SIGN_IN)

        // Inform the presenter that the userDetails is logging in
        presenter.onUserLoggingIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            // We've received sign in response so inform presenter
            val response = IdpResponse.fromResultIntent(data)
            presenter.onSignInComplete(response, resultCode)
        }
    }

    override fun showLearnerDetailsForm() {
        if (viewContainer.currentViewBinding is ViewLearnerDetailsFormBinding)
            return

        val viewBinding = ViewLearnerDetailsFormBinding.inflate(LayoutInflater.from(this)).apply {
            this.continueBtn.setOnClickListener {
                val forename = this.foreNameTxt.text
                val surname = this.surnameTxt.text
                val dobDay = this.dobPicker.dayOfMonth
                val dobMonth = this.dobPicker.month
                val dobYear = this.dobPicker.year

                presenter.onLearnerDetailsProvided(forename, surname, dobDay, dobMonth, dobYear)
            }
        }

        viewContainer.transitionToScene(viewBinding, TAG_LEARNER_DETAILS_FORM)
    }

    override fun showInstructorDetailsForm() {
        if (viewContainer.currentViewBinding is ViewInstructorDetailsFormBinding)
            return

        val viewBinding = ViewInstructorDetailsFormBinding.inflate(LayoutInflater.from(this)).apply {
            this.continueBtn.setOnClickListener {
                this.continueBtn.setOnClickListener {
                    val forename = this.foreNameTxt.text
                    val surname = this.surnameTxt.text
                    val dobDay = this.dobPicker.dayOfMonth
                    val dobMonth = this.dobPicker.month
                    val dobYear = this.dobPicker.year

                    presenter.onInstructorDetailsProvided(forename, surname, dobDay, dobMonth, dobYear)
                }
            }
        }

        viewContainer.transitionToScene(viewBinding, TAG_INSTRUCTOR_DETAILS_FORM)
    }

    override fun showSelectLearnerTimes() {
        if (viewContainer.currentViewBinding is ViewLearnerTimesBinding)
            return

        val viewBinding = ViewLearnerTimesBinding.inflate(LayoutInflater.from(this)).apply {
            this.sundayBtn.setOnClickListener {
                this.sundayBtn.isSelected = !this.sundayBtn.isSelected
            }
            this.mondayBtn.setOnClickListener {
                this.mondayBtn.isSelected = !this.mondayBtn.isSelected
            }
            this.tuesdayBtn.setOnClickListener {
                this.tuesdayBtn.isSelected = !this.tuesdayBtn.isSelected
            }
            this.wednesdayBtn.setOnClickListener {
                this.wednesdayBtn.isSelected = !this.wednesdayBtn.isSelected
            }
            this.thursdayBtn.setOnClickListener {
                this.thursdayBtn.isSelected = !this.thursdayBtn.isSelected
            }
            this.fridayBtn.setOnClickListener {
                this.fridayBtn.isSelected = !this.fridayBtn.isSelected
            }
            this.saturdayBtn.setOnClickListener {
                this.saturdayBtn.isSelected = !this.saturdayBtn.isSelected
            }
            this.continueBtn.setOnClickListener {
                val availableFromHour = this.startOfAvailabilityPicker.hour
                val availableFromMinute = this.startOfAvailabilityPicker.minute
                val availableToHour = this.endOfAvailabilityPicker.hour
                val availableToMinute = this.endOfAvailabilityPicker.minute

                presenter.onLearnerTimesProvided(availableFromHour, availableFromMinute, availableToHour, availableToMinute,
                    this.sundayBtn.isSelected, this.mondayBtn.isSelected, this.tuesdayBtn.isSelected,
                    this.wednesdayBtn.isSelected, this.thursdayBtn.isSelected, this.fridayBtn.isSelected,
                    this.saturdayBtn.isSelected)
            }
        }

        viewContainer.transitionToScene(viewBinding, TAG_LEARNER_TIMES)
    }

    override fun showSelectInstructorTimes() {
        if (viewContainer.currentViewBinding is ViewInstructorTimesBinding)
            return

        val viewBinding = ViewInstructorTimesBinding.inflate(LayoutInflater.from(this)).apply {
            this.sundayBtn.setOnClickListener {
                this.sundayBtn.isSelected = !this.sundayBtn.isSelected
            }
            this.mondayBtn.setOnClickListener {
                this.mondayBtn.isSelected = !this.mondayBtn.isSelected
            }
            this.tuesdayBtn.setOnClickListener {
                this.tuesdayBtn.isSelected = !this.tuesdayBtn.isSelected
            }
            this.wednesdayBtn.setOnClickListener {
                this.wednesdayBtn.isSelected = !this.wednesdayBtn.isSelected
            }
            this.thursdayBtn.setOnClickListener {
                this.thursdayBtn.isSelected = !this.thursdayBtn.isSelected
            }
            this.fridayBtn.setOnClickListener {
                this.fridayBtn.isSelected = !this.fridayBtn.isSelected
            }
            this.saturdayBtn.setOnClickListener {
                this.saturdayBtn.isSelected = !this.saturdayBtn.isSelected
            }
            this.continueBtn.setOnClickListener {
                val availableFromHour = this.startOfAvailabilityPicker.hour
                val availableFromMinute = this.startOfAvailabilityPicker.minute
                val availableToHour = this.endOfAvailabilityPicker.hour
                val availableToMinute = this.endOfAvailabilityPicker.minute

                presenter.onInstructorTimesProvided(availableFromHour, availableFromMinute, availableToHour, availableToMinute,
                    this.sundayBtn.isSelected, this.mondayBtn.isSelected, this.tuesdayBtn.isSelected,
                    this.wednesdayBtn.isSelected, this.thursdayBtn.isSelected, this.fridayBtn.isSelected,
                    this.saturdayBtn.isSelected)
            }
        }

        viewContainer.transitionToScene(viewBinding, TAG_INTSTRUCTOR_TIMES)
    }

    override fun showProgress(message: CharSequence) {
        if (viewContainer.isViewShowing(TAG_PROGRESS)) {
            (viewContainer.currentViewBinding as? ViewProgressBinding)?.progressTxt?.text = message
        } else {
            val viewBinding = ViewProgressBinding.inflate(LayoutInflater.from(this)).apply {
                this.progressTxt.text = message
            }
            viewContainer.transitionToScene(viewBinding, TAG_PROGRESS)
        }
    }

    override fun navigateToMain() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        while (postponedRunnables.isNotEmpty()) {
            postponedRunnables.pop().invoke()
        }
    }
}