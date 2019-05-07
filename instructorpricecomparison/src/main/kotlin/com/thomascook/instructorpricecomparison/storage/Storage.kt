package com.thomascook.instructorpricecomparison.storage

import com.thomascook.instructorpricecomparison.Instructor
import com.thomascook.instructorpricecomparison.Learner
import io.reactivex.Single

/**
 * Contract for interacting with storage
 */
interface Storage {
    fun addLearner(learner: Learner) : Single<Learner>
    fun addInstructor(instructor: Instructor) : Single<Instructor>
}