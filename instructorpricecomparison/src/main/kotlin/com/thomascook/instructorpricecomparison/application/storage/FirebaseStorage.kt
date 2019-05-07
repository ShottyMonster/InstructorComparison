package com.thomascook.instructorpricecomparison.application.storage

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.thomascook.instructorpricecomparison.Instructor
import com.thomascook.instructorpricecomparison.Learner
import com.thomascook.instructorpricecomparison.UserDetails
import com.thomascook.instructorpricecomparison.application.EventReporter
import com.thomascook.instructorpricecomparison.storage.Storage
import io.reactivex.Single
import java.util.*
import kotlin.collections.HashMap

class FirebaseStorage : Storage {
    // Reference to fire base storage
    private val firebase = FirebaseFirestore.getInstance()

    private fun Learner.toFirestoreHashmap() : HashMap<String, Any> {
        val learnerMap = HashMap<String, Any>()
        learnerMap["uid"] = this.userDetails.uid ?: ""
        learnerMap["forename"] = this.forename
        learnerMap["surname"] = this.surname
        learnerMap["dateOfBirth"] = this.dateOfBirth.toString()
        learnerMap["availableFromHour"] = this.availableFromHour
        learnerMap["availableFromMinute"] = this.availableFromMinute
        learnerMap["availableToHour"] = this.availableToHour
        learnerMap["availableToMinute"] = this.availableToMinute
        learnerMap["sunday"] = this.sunday
        learnerMap["monday"] = this.monday
        learnerMap["tuesday"] = this.tuesday
        learnerMap["wednesday"] = this.wednesday
        learnerMap["thursday"] = this.thursday
        learnerMap["friday"] = this.friday
        learnerMap["saturday"] = this.saturday
        return learnerMap
    }

    private fun Instructor.toFirestoreHashmap() : HashMap<String, Any> {
        val instructorMap = HashMap<String, Any>()
        instructorMap["uid"] = this.userDetails.uid ?: ""
        instructorMap["forename"] = this.forename
        instructorMap["surname"] = this.surname
        instructorMap["dateOfBirth"] = this.dateOfBirth.toString()
        instructorMap["availableFromHour"] = this.availableFromHour
        instructorMap["availableFromMinute"] = this.availableFromMinute
        instructorMap["availableToHour"] = this.availableToHour
        instructorMap["availableToMinute"] = this.availableToMinute
        instructorMap["sunday"] = this.sunday
        instructorMap["monday"] = this.monday
        instructorMap["tuesday"] = this.tuesday
        instructorMap["wednesday"] = this.wednesday
        instructorMap["thursday"] = this.thursday
        instructorMap["friday"] = this.friday
        instructorMap["saturday"] = this.saturday
        return instructorMap
    }

    override fun addLearner(learner: Learner): Single<Learner> {
        return Single.create { emitter ->
            val learnerMap = learner.toFirestoreHashmap()
            firebase.collection("learners")
                .add(learnerMap)
                .addOnSuccessListener {
                    val newLearner = learner.copy(uid = it.id)
                    emitter.onSuccess(newLearner)
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun addInstructor(instructor: Instructor): Single<Instructor> {
        return Single.create { emitter ->
            val learnerMap = instructor.toFirestoreHashmap()
            firebase.collection("instructors")
                .add(learnerMap)
                .addOnSuccessListener {
                    val newLearner = instructor.copy(uid = it.id)
                    emitter.onSuccess(newLearner)
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }
}