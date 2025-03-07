package com.example.applikasjons_avokadoene.models

import com.google.firebase.Timestamp
import java.io.Serializable



data class Course(
    var code: String = "",
    var name: String = "",
    var instructor: String = "",
    var studentsEnrolled: Int = 0,
    var id: String = "",
    var description: String = "",
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null

) : Serializable {

    /**
     * Convert Course object to HashMap for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "name" to name,
            "code" to code,
            "instructor" to instructor,
            "studentsEnrolled" to studentsEnrolled,
            "createdAt" to Timestamp.now(),
            "description" to description,
            "updatedAt" to Timestamp.now()
        )
    }
}
