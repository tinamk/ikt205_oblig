package com.example.applikasjons_avokadoene.models

import com.google.firebase.Timestamp
import java.io.Serializable


data class Student(
    var id: String = "",
    var name: String = "",
    var studentId: String = "",
    var email: String = "",
    var phone: String = "",
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
) : Serializable {

    /**
     * Convert Student object to HashMap for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "name" to name,
            "studentId" to studentId,
            "email" to email,
            "phone" to phone,
            "updatedAt" to Timestamp.now()
        )
    }
}