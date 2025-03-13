package com.example.applikasjons_avokadoene.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Course(
    @DocumentId var id: String = "",
    val name: String = "",
    val code: String = "",
    val instructor: String = "",
    val studentsEnrolled: Int = 0,
    @Exclude val grades: MutableList<String> = mutableListOf(),
    @Exclude var averageGrade: String = "N/A",
    val createdAt: Timestamp? = Timestamp.now(),
    var updatedAt: Timestamp? = Timestamp.now()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createStringArrayList() ?: mutableListOf(),
        parcel.readString() ?: "N/A"
    )

    fun calculateAverageGrade() {
        if (grades.isEmpty()) {
            averageGrade = "N/A"
            return
        }

        val gradeValues = grades.map { grade ->
            when (grade) {
                "A" -> 5.0
                "B" -> 4.0
                "C" -> 3.0
                "D" -> 2.0
                "E" -> 1.0
                "F" -> 0.0
                else -> 0.0
            }
        }

        val average = gradeValues.average()

        averageGrade = when {
            average >= 4.5 -> "A"
            average >= 3.5 -> "B"
            average >= 2.5 -> "C"
            average >= 1.5 -> "D"
            average >= 0.5 -> "E"
            else -> "F"
        }
    }
    
    /**
     * Convert Course object to HashMap for Firestore
     */
    fun toMap(): Map<String, Any?> {
        val map = hashMapOf<String, Any?>(
            "name" to name,
            "code" to code,
            "instructor" to instructor,
            "studentsEnrolled" to studentsEnrolled
        )
        
        // Only add timestamps if they're not null
        if (createdAt != null) {
            map["createdAt"] = createdAt
        }
        
        // Always use current timestamp for updates
        map["updatedAt"] = Timestamp.now()
        
        return map
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(code)
        parcel.writeString(instructor)
        parcel.writeInt(studentsEnrolled)
        parcel.writeStringList(grades)
        parcel.writeString(averageGrade)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Course> {
        override fun createFromParcel(parcel: Parcel): Course {
            return Course(parcel)
        }

        override fun newArray(size: Int): Array<Course?> {
            return arrayOfNulls(size)
        }
    }
}