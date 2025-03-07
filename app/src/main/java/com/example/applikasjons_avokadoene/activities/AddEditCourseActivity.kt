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

class AddEditCourseActivity : AppCompatActivity() {

    private lateinit var editTextCourseName: EditText
    private lateinit var editTextCourseCode: EditText
    private lateinit var editTextInstructor: EditText
    private lateinit var editTextStudentsEnrolled: EditText
    private lateinit var buttonSaveCourse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_course)

        // Initialize UI elements
        editTextCourseName = findViewById(R.id.editTextCourseName)
        editTextCourseCode = findViewById(R.id.editTextCourseCode)
        editTextInstructor = findViewById(R.id.editTextInstructor)
        editTextStudentsEnrolled = findViewById(R.id.editTextStudentsEnrolled)
        buttonSaveCourse = findViewById(R.id.buttonSaveCourse)

        // Set click listener
        buttonSaveCourse.setOnClickListener {
            saveCourse()
        }
    }

    private fun saveCourse() {
        val courseName = editTextCourseName.text.toString()
        val courseCode = editTextCourseCode.text.toString()
        val instructor = editTextInstructor.text.toString()
        val studentsEnrolled = editTextStudentsEnrolled.text.toString().toIntOrNull()

        if (courseName.isEmpty() || courseCode.isEmpty() || instructor.isEmpty() || studentsEnrolled == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val newCourse = Course(courseName, courseCode, instructor, studentsEnrolled)

        val resultIntent = Intent()
        resultIntent.putExtra("newCourse", newCourse)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()


    }
}
