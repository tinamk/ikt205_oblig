package com.example.applikasjons_avokadoene.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Grade
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp

class AddGradeActivity : AppCompatActivity() {

    private lateinit var editTextStudentId: EditText
    private lateinit var editTextCourseCode: EditText
    private lateinit var editTextGrade: EditText
    private lateinit var buttonSaveGrade: Button
    
    private var studentId: String? = null
    private var studentName: String? = null
    private var courseId: String? = null
    private var courseName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grade)
        title = "Add Grade"

        // Initialize views
        editTextStudentId = findViewById(R.id.editTextStudentId)
        editTextCourseCode = findViewById(R.id.editTextCourseCode)
        editTextGrade = findViewById(R.id.editTextGrade)
        buttonSaveGrade = findViewById(R.id.buttonSaveGrade)

        // Get data from intent
        studentId = intent.getStringExtra("STUDENT_ID")
        studentName = intent.getStringExtra("STUDENT_NAME")
        courseId = intent.getStringExtra("COURSE_ID")
        courseName = intent.getStringExtra("COURSE_NAME")
        
        // Pre-fill fields if data is available
        if (studentId != null && studentName != null) {
            editTextStudentId.setText(studentName)
            editTextStudentId.isEnabled = false
        }
        
        if (courseId != null && courseName != null) {
            editTextCourseCode.setText(courseName)
            editTextCourseCode.isEnabled = false
        }

        // Set up save button
        buttonSaveGrade.setOnClickListener {
            saveGrade()
        }
    }

    private fun saveGrade() {
        val gradeText = editTextGrade.text.toString().trim().uppercase()
        
        // Validate grade
        if (gradeText.isEmpty() || !gradeText.matches(Regex("[A-F]"))) {
            Toast.makeText(this, "Please enter a valid grade (A-F)", Toast.LENGTH_SHORT).show()
            return
        }
        
        // If we don't have student or course ID, we need to look them up
        if (studentId == null || courseId == null) {
            Toast.makeText(this, "Please select a student and course first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create grade object
        val grade = Grade(
            studentId = studentId!!,
            studentName = studentName!!,
            courseId = courseId!!,
            courseName = courseName!!,
            score = convertGradeToScore(gradeText),
            date = Timestamp.now()
        )
        
        // Save to Firestore
        FirebaseUtil.getGradesCollection()
            .add(grade.toMap())
            .addOnSuccessListener {
                Toast.makeText(this, "Grade added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding grade: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun convertGradeToScore(grade: String): Double {
        return when (grade) {
            "A" -> 5.0
            "B" -> 4.0
            "C" -> 3.0
            "D" -> 2.0
            "E" -> 1.0
            "F" -> 0.0
            else -> 0.0
        }
    }
}
