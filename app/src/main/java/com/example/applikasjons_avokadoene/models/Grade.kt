package com.example.applikasjons_avokadoene.models



import com.google.firebase.Timestamp
import java.io.Serializable


data class Grade(
    var id: String = "",
    var studentId: String = "",
    var studentName: String = "",
    var courseId: String = "",
    var courseName: String = "",
    var grade: String = "",
    var score: Double = 0.0,
    var date: Timestamp = Timestamp.now()
) : Serializable {

    /**
     * Convert Grade object to HashMap for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "studentId" to studentId,
            "studentName" to studentName,
            "courseId" to courseId,
            "courseName" to courseName,
            "grade" to grade,
            "score" to score,
            "date" to date
        )
    }
}