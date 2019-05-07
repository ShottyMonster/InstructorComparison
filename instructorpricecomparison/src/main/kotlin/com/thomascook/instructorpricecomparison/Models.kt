package com.thomascook.instructorpricecomparison

import java.io.Serializable
import java.util.*


interface WalkthroughSteps

enum class INSTRUCTOR_STEPS : WalkthroughSteps {
    PROVIDE_DETAILS, COMPLETED
}

enum class LEARNER_STEPS : WalkthroughSteps {
    PROVIDE_DETAILS, COMPLETED
}

/**
 * Type representing a users details in the system
 */
data class UserDetails(val uid: String? = null,
                       val currentWalkthroughStep: WalkthroughSteps? = null,
                       val isCurrentlyLoggingIn: Boolean = false) : Serializable

/**
 * Base interface for learners and instructors
 */
interface User {
    val userDetails: UserDetails
    val uid: String
    val forename: String
    val surname: String
    val dateOfBirth: Date
    val availableFromHour: Int
    val availableFromMinute: Int
    val availableToHour: Int
    val availableToMinute: Int
    val sunday: Boolean
    val monday: Boolean
    val tuesday: Boolean
    val wednesday: Boolean
    val thursday: Boolean
    val friday: Boolean
    val saturday: Boolean
}

/**
 * Type representing a learner in the system
 */
data class Learner(override val userDetails: UserDetails,
                   override val uid: String,
                   override val forename: String,
                   override val surname: String,
                   override val dateOfBirth: Date,
                   override val availableFromHour: Int = 0,
                   override val availableFromMinute: Int = 0,
                   override val availableToHour: Int = 0,
                   override val availableToMinute: Int = 0 ,
                   override val sunday: Boolean = false,
                   override val monday: Boolean = false,
                   override val tuesday: Boolean = false,
                   override val wednesday: Boolean = false,
                   override val thursday: Boolean = false,
                   override val friday: Boolean = false,
                   override val saturday: Boolean = false) : User

/**
 * Type representing an instructor in the system
 */
data class Instructor(override val userDetails: UserDetails,
                      override val uid: String,
                      override val forename: String,
                      override val surname: String,
                      override val dateOfBirth: Date,
                      override val availableFromHour: Int = 0,
                      override val availableFromMinute: Int = 0,
                      override val availableToHour: Int = 0,
                      override val availableToMinute: Int = 0 ,
                      override val sunday: Boolean = false,
                      override val monday: Boolean = false,
                      override val tuesday: Boolean = false,
                      override val wednesday: Boolean = false,
                      override val thursday: Boolean = false,
                      override val friday: Boolean = false,
                      override val saturday: Boolean = false) : User