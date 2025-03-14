package com.example.applikasjons_avokadoene.utils

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Firebase utility class
object FirebaseUtil {
    // Collection names as constants
    const val COLLECTION_STUDENTS = "students"
    const val COLLECTION_COURSES = "courses"
    const val COLLECTION_GRADES = "grades"
    
    // Get Firestore instance
    val db: FirebaseFirestore = Firebase.firestore

    //
    fun getStudentsCollection(): CollectionReference {
        val collection = db.collection(COLLECTION_STUDENTS)
        android.util.Log.d("FirebaseUtil", "Getting reference to students collection")
        return collection
    }

    // Get reference to courses collection
    fun getCoursesCollection(): CollectionReference {
        android.util.Log.d("FirebaseUtil", "Getting reference to courses collection")
        val collectionPath = COLLECTION_COURSES
        android.util.Log.d("FirebaseUtil", "Collection path: $collectionPath")
        return db.collection(collectionPath)
    }

   // Get reference to grades collection
    fun getGradesCollection(): CollectionReference {
        android.util.Log.d("FirebaseUtil", "Getting reference to grades collection")
        return db.collection(COLLECTION_GRADES)
    }
}