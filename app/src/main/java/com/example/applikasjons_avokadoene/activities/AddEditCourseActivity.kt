package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Course
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp


// Activity for adding or editing a course
class AddEditCourseActivity : AppCompatActivity() {

    private lateinit var editTextCourseName: EditText
    private lateinit var editTextCourseCode: EditText
    private lateinit var editTextInstructor: EditText
    private lateinit var editTextStudentsEnrolled: EditText
    private lateinit var buttonSaveCourse: Button
    
    private var isEditMode = false
    private var courseId = ""
    private var existingGrades = mutableListOf<String>()
    private var existingAverageGrade = "N/A"
    
    companion object {
        const val RESULT_COURSE_ADDED = 100
        const val RESULT_COURSE_UPDATED = 101
    }

    // Initialize UI elements and set click listener for save button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_course)

        // Set title based on mode
        title = if (intent.hasExtra("COURSE")) "Edit Course" else "Add Course"

        // Initialize UI elements
        editTextCourseName = findViewById(R.id.editTextCourseName)
        editTextCourseCode = findViewById(R.id.editTextCourseCode)
        editTextInstructor = findViewById(R.id.editTextInstructor)
        editTextStudentsEnrolled = findViewById(R.id.editTextStudentsEnrolled)
        buttonSaveCourse = findViewById(R.id.buttonSaveCourse)

        // Check if we're in edit mode
        if (intent.hasExtra("COURSE")) {
            isEditMode = true
            val course = intent.getParcelableExtra<Course>("COURSE")
            if (course != null) {
                courseId = course.id
                existingGrades = course.grades
                existingAverageGrade = course.averageGrade
                
                // Populate fields with existing data
                editTextCourseName.setText(course.name)
                editTextCourseCode.setText(course.code)
                editTextInstructor.setText(course.instructor)
                editTextStudentsEnrolled.setText(course.studentsEnrolled.toString())
            }
        }

        // Set click listener
        buttonSaveCourse.setOnClickListener {
            saveCourse()
        }
    }

    // Save course to Firebase
    private fun saveCourse() {
        val courseName = editTextCourseName.text.toString().trim()
        val courseCode = editTextCourseCode.text.toString().trim()
        val instructor = editTextInstructor.text.toString().trim()
        val studentsEnrolled = editTextStudentsEnrolled.text.toString().toIntOrNull()

        if (courseName.isEmpty() || courseCode.isEmpty() || instructor.isEmpty() || studentsEnrolled == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode) {
            // Update existing course
            val updatedCourse = Course(
                id = courseId,
                name = courseName,
                code = courseCode,
                instructor = instructor,
                studentsEnrolled = studentsEnrolled,
                grades = existingGrades,
                averageGrade = existingAverageGrade,
                updatedAt = Timestamp.now()
            )
            
            FirebaseUtil.getCoursesCollection().document(courseId)
                .update(updatedCourse.toMap())
                .addOnSuccessListener {
                    Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_COURSE_UPDATED)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating course: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Create new course
            val newCourse = Course(
                name = courseName,
                code = courseCode,
                instructor = instructor,
                studentsEnrolled = studentsEnrolled,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            // Add to Firestore
            FirebaseUtil.getCoursesCollection()
                .add(newCourse.toMap())
                .addOnSuccessListener { documentRef ->
                    Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_COURSE_ADDED)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding course: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}