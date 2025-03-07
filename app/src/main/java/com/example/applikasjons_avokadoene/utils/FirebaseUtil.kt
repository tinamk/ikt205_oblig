package com.example.applikasjons_avokadoene.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Utility class for Firebase operations
 */
object FirebaseUtil {
    // Collection names
    const val COLLECTION_STUDENTS = "students"
    const val COLLECTION_COURSES = "courses"
    const val COLLECTION_GRADES = "grades"

    // Get Firestore instance
    val db: FirebaseFirestore = Firebase.firestore

    /**
     * Get reference to students collection
     */
    fun getStudentsCollection() = db.collection(COLLECTION_STUDENTS)

    /**
     * Get reference to courses collection
     */
    fun getCoursesCollection() = db.collection(COLLECTION_COURSES)

    /**
     * Get reference to grades collection
     */
    fun getGradesCollection() = db.collection(COLLECTION_GRADES)
}